package com.iso.hypo.finance.application.usecase.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.enumeration.PaymentMethodEnumDto;
import com.iso.hypo.common.application.port.PaymentProviderConfigurationEntry;
import com.iso.hypo.common.application.port.PaymentProviderConfigurationPort;
import com.iso.hypo.finance.application.dto.PaymentDto;
import com.iso.hypo.finance.application.exception.PaymentException;
import com.iso.hypo.finance.application.mapper.PaymentDtoMapper;
import com.iso.hypo.finance.application.port.BrandServicePort;
import com.iso.hypo.finance.application.port.MemberServicePort;
import com.iso.hypo.finance.application.port.PaymentProviderPort;
import com.iso.hypo.finance.application.port.dto.BrandRef;
import com.iso.hypo.finance.application.port.dto.MemberRef;
import com.iso.hypo.finance.application.port.dto.paymentprovider.CreditCardRef;
import com.iso.hypo.finance.application.port.dto.paymentprovider.ReceiptRef;
import com.iso.hypo.finance.application.port.mapper.CreditCardRefMapper;
import com.iso.hypo.finance.application.usecase.PaymentService;
import com.iso.hypo.finance.domain.model.FinancialInstrument;
import com.iso.hypo.finance.domain.model.Payment;
import com.iso.hypo.finance.domain.model.enumeration.PaymentStatusEnum;
import com.iso.hypo.finance.domain.repository.FinancialInstrumentRepository;
import com.iso.hypo.finance.domain.repository.PaymentRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

	private final BrandServicePort brandServicePort;
	
	private final MemberServicePort memberServicePort;

	private final PaymentDtoMapper paymentMapper;
	
	private final CreditCardRefMapper creditCardRefMapper;

	private final PaymentRepository paymentRepository;
	
	private final PaymentProviderConfigurationPort paymentProviderConfigurationPort;
	
	private final PaymentProviderPort paymentProviderPort;
	
	private final FinancialInstrumentRepository financialInstrumentRepository;

	@Value("${app.test.run:false}")
	private boolean testRun;

	private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

	private final RequestContext requestContext;

	public PaymentServiceImpl(
			PaymentRepository paymentRepository,
			BrandServicePort brandValidationPort,
			MemberServicePort memberServicePort,
			FinancialInstrumentRepository financialInstrumentRepository,
			PaymentProviderConfigurationPort paymentProviderConfigurationPort,
			PaymentProviderPort paymentProviderPort,
			PaymentDtoMapper paymentMapper,
			CreditCardRefMapper creditCardRefMapper,
			RequestContext requestContext) {
		this.paymentRepository = paymentRepository;
		this.brandServicePort = brandValidationPort;
		this.memberServicePort = memberServicePort;
		this.financialInstrumentRepository = financialInstrumentRepository;
		this.paymentProviderConfigurationPort = paymentProviderConfigurationPort;
		this.paymentProviderPort = paymentProviderPort;
		this.paymentMapper = paymentMapper;
		this.creditCardRefMapper = creditCardRefMapper;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public PaymentDto purchase(PaymentDto paymentDto) throws PaymentException {

		try {
			Assert.notNull(paymentDto, "paymentDto must not be null");

			Payment payment = paymentMapper.toEntity(paymentDto);
			
			// Validate payment details
			validatePayment(payment);
			
			// Resolve dependencies
			BrandRef brand = resolveBrand(paymentDto.getBrandUuid());
			MemberRef member = resolveMember(brand.getUuid(), paymentDto.getMemberUuid());
			FinancialInstrument financialInstrument = resolveFinancialInstrument(brand.getUuid(), member.getUuid(), paymentDto.getFinancialInstrumentUuid());
		
			// Validate financial instrument details
			validateFinancialInstrument(payment, financialInstrument);
			
			payment.setUuid(UUID.randomUUID().toString());
			payment.setCreatedOn(Instant.now());
			payment.setCreatedBy(requestContext.getUsername());
			payment.setPaymentStatus(PaymentStatusEnum.idle);
			
			Payment saved = paymentRepository.save(payment);
			
			// Call CreditCard provider service to process the payment
			PaymentProviderConfigurationEntry paymentProviderConfig = paymentProviderConfigurationPort.getPaymentProviderConfiguration(brand.getCode());
			
			CreditCardRef creditCardRef = creditCardRefMapper.toRef(financialInstrument.getCreditCard());
			
			ReceiptRef receipt =  paymentProviderPort.purchase(paymentProviderConfig, requestContext, creditCardRef, payment.getOrderNumber(), 
											payment.getMemberUuid(), 
											payment.getPurchaseAmount().getAmount(),
											payment.getPurchaseAmount().getCurrency().getCode());

			payment.setPaymentServiceProviderRawResponse(receipt.getProviderRawResponse());
			
			if (receipt.isApproved()) {
				payment.setTransactionId(receipt.getTxnNumber());
				payment.setTransactionReference(receipt.getReferenceNum());
				payment.setPaymentId(receipt.getPaymentId());
				payment.setGatewayResponse(receipt.getMessage());
				payment.setGatewayResponseCode(receipt.getProviderResponseCode());
				payment.setGatewayISOResponseCode(receipt.getISO());
				payment.setPaymentStatus(PaymentStatusEnum.completed);
				payment.setPaidOn(Instant.now());
			} else {
				payment.setGatewayResponse(receipt.getMessage());
				payment.setGatewayResponseCode(receipt.getProviderResponseCode());
				payment.setGatewayISOResponseCode(receipt.getISO());
				payment.setPaymentStatus(PaymentStatusEnum.failed);
			}

			saved = paymentRepository.save(payment);
			
			return paymentMapper.toDto(saved);
		} catch (Exception e) {
			if (paymentDto != null) {
				logger.error("Error - brandUuid={} memberUuid={}  orderUuid={} orderNumber={}", paymentDto.getBrandUuid(), paymentDto.getMemberUuid(), paymentDto.getOrderUuid(), paymentDto.getOrderNumber(), e);
			}
			
			if (e instanceof PaymentException) {
				throw (PaymentException) e;
			}
			throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.CREATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public void refund(String brandUuid, String memberUuid, String paymentUuid) throws PaymentException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			MemberRef member = resolveMember(brand.getUuid(), memberUuid);

			Payment entity = this.readByPaymentUuid(brand.getUuid(), member.getUuid(), paymentUuid);
			
			// TODO: Call CreditCard provider service to refund

			paymentRepository.save(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, paymentUuid={}, uuid={}", brandUuid, paymentUuid, paymentUuid, e);

			if (e instanceof PaymentException) {
				throw (PaymentException) e;
			}
			throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByBrandUuid(String brandUuid) throws PaymentException {
		try {
			if (brandServicePort.brandDeleted(brandUuid)) {
				long deletedCount = paymentRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());

				logger.info("Payment deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);
			}
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);

			throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByMemberUuid(String brandUuid, String memberUuid) throws PaymentException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			
			if (memberServicePort.memberDeleted(brand.getUuid(), memberUuid)) {
				long deletedCount = paymentRepository.deleteAllByMemberUuid(brandUuid, memberUuid, requestContext.getUsername());

				logger.info("Payment deleted for member - brandUuid={} memberUuid={} deletedCount={} ", brandUuid, memberUuid, deletedCount);
			}
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);

			throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.DELETE_FAILED, e);
		}
	}
	
	private boolean validatePayment(Payment payment) throws PaymentException {
		
		if (payment.getPurchaseAmount() == null || payment.getPurchaseAmount().getAmount() <= 0) {
			 throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.INVALID_AMOUNT,
					 "Payment amount must be greater than zero");
		}
		
		if (payment.getMemberUuid() == null || payment.getMemberUuid().isEmpty()) {
			 throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.MISSING_MEMBER_INFORMATION,
					 "Member information not found for the payment");
		}
		
		if (payment.getOrderNumber() == null || payment.getOrderNumber().isEmpty()) {
			 throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.MISSING_ORDER_INFORMATION,
					 "Order Number information not found for the payment");
		}
		
		if (payment.getOrderUuid() == null || payment.getOrderUuid().isEmpty()) {
			 throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.MISSING_ORDER_INFORMATION,
					 "Order Uuid information not found for the payment");
		}
		
		if (payment.getFinancialInstrumentUuid() == null || payment.getFinancialInstrumentUuid().isEmpty()) {
			 throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.MISSING_FINANCIAL_INSTRUMENT_INFORMATION,
					 "Member information not found for the payment");
		}
		
		return true;
	}
	
	private boolean validateFinancialInstrument(Payment payment, FinancialInstrument financialInstrument) throws PaymentException {
		
		if (payment.getPaymentMethod() == PaymentMethodEnumDto.credit) {
			if (financialInstrument.getCreditCard() == null) {
				 throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.MISSING_CREDIT_CARD_INFORMATION,
						 "Credit card information not found for the financial instrument");
			}
		}
		
		if (payment.getPaymentMethod() == PaymentMethodEnumDto.debit) {
			if (financialInstrument.getBankAccount() == null) {
				 throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.MISSING_CREDIT_CARD_INFORMATION,
						 "Bank information not found for the financial instrument");
			}
		}
		
		return true;
	}

	private Payment readByPaymentUuid(String brandUuid, String memberUuid, String paymentUuid) throws PaymentException {
		Optional<Payment> entity = paymentRepository.findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid, paymentUuid);
		if (entity.isEmpty()) {
			throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.FINANCIAL_INSTRUMENT_NOT_FOUND,
					"Payment not found");
		}

		return entity.get();
	}
	
	private BrandRef resolveBrand(String brandUuid) throws PaymentException {
		return brandServicePort.find(brandUuid)
				.orElseThrow(() -> new PaymentException(requestContext.getTrackingNumber(), PaymentException.BRAND_NOT_FOUND,
				"Brand not found - brandUuid=" + brandUuid));
	}
	
	private MemberRef resolveMember(String brandUuid, String memberUuid) throws PaymentException {
		return  memberServicePort.find(brandUuid, memberUuid)
				.orElseThrow(() -> new PaymentException(requestContext.getTrackingNumber(), PaymentException.MEMBER_NOT_FOUND,
				"Member not found - brandUuid=" + brandUuid + " memberUuid=" + memberUuid));
	}
	
	private FinancialInstrument resolveFinancialInstrument(String brandUuid, String memberUuid, String uuid) throws PaymentException {
		try {
			return financialInstrumentRepository.findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid, uuid)
					.orElseThrow(() -> new PaymentException(requestContext.getTrackingNumber(), PaymentException.FINANCIAL_INSTRUMENT_NOT_FOUND,
							"Financial instrument not found - financialInstrumentUuid=" + uuid));
		} catch (Exception e) {
			logger.error("Error - brandUuid={} memberUuid={} financialInstrumentUuid={} ", brandUuid, memberUuid, uuid, e);

			throw new PaymentException(requestContext.getTrackingNumber(), PaymentException.FINANCIAL_INSTRUMENT_NOT_FOUND, e);
		}
	}
}