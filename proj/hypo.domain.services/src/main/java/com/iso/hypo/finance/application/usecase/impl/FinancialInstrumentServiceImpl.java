package com.iso.hypo.finance.application.usecase.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.contact.PhoneNumberDto;
import com.iso.hypo.common.application.dto.enumeration.PhoneNumberTypeEnumDto;
import com.iso.hypo.common.application.port.PaymentProviderConfigurationEntry;
import com.iso.hypo.common.application.port.PaymentProviderConfigurationPort;
import com.iso.hypo.finance.application.dto.FinancialInstrumentDto;
import com.iso.hypo.finance.application.exception.FinancialInstrumentException;
import com.iso.hypo.finance.application.mapper.FinancialInstrumentDtoMapper;
import com.iso.hypo.finance.application.port.BrandServicePort;
import com.iso.hypo.finance.application.port.MemberServicePort;
import com.iso.hypo.finance.application.port.PaymentProviderPort;
import com.iso.hypo.finance.application.port.dto.BrandRef;
import com.iso.hypo.finance.application.port.dto.MemberRef;
import com.iso.hypo.finance.application.port.dto.paymentprovider.CreditCardRef;
import com.iso.hypo.finance.application.port.dto.paymentprovider.ReceiptRef;
import com.iso.hypo.finance.application.usecase.FinancialInstrumentService;
import com.iso.hypo.finance.domain.model.FinancialInstrument;
import com.iso.hypo.finance.domain.repository.FinancialInstrumentRepository;
import com.iso.hypo.finance.infrastructure.port.mapper.FinanceCreditCardRefMapper;

@Service
public class FinancialInstrumentServiceImpl implements FinancialInstrumentService {

	private final BrandServicePort brandServicePort;
	
	private final MemberServicePort memberServicePort;

	private final PaymentProviderConfigurationPort paymentProviderConfigurationPort;
	
	private final PaymentProviderPort paymentProviderPort;
	
	private final FinancialInstrumentDtoMapper financialInstrumentMapper;
	
	private final FinanceCreditCardRefMapper creditCardRefMapper;

	private final FinancialInstrumentRepository financialInstrumentRepository;

	private static final Logger logger = LoggerFactory.getLogger(FinancialInstrumentServiceImpl.class);

	private final RequestContext requestContext;

	public FinancialInstrumentServiceImpl(
			FinancialInstrumentRepository financialInstrumentRepository,
			BrandServicePort brandValidationPort,
			MemberServicePort memberServicePort,
			PaymentProviderConfigurationPort paymentProviderConfigurationPort,
			PaymentProviderPort paymentProviderPort,
			FinancialInstrumentDtoMapper financialInstrumentMapper,
			FinanceCreditCardRefMapper creditCardRefMapper,
			RequestContext requestContext) {
	
		this.financialInstrumentRepository = financialInstrumentRepository;
		this.brandServicePort = brandValidationPort;
		this.memberServicePort = memberServicePort;
		this.paymentProviderConfigurationPort = paymentProviderConfigurationPort;
		this.paymentProviderPort = paymentProviderPort;
		this.financialInstrumentMapper = financialInstrumentMapper;
		this.creditCardRefMapper = creditCardRefMapper;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public FinancialInstrumentDto create(FinancialInstrumentDto financialInstrumentDto) throws FinancialInstrumentException {
		try {
			Assert.notNull(financialInstrumentDto, "financialInstrumentDto must not be null");

			FinancialInstrument financialInstrument = financialInstrumentMapper.toEntity(financialInstrumentDto);

			BrandRef brand = resolveBrand(financialInstrumentDto.getBrandUuid());
			MemberRef member = resolveMember(brand.getUuid(), financialInstrumentDto.getMemberUuid());

			CreditCardRef creditCardRef = creditCardRefMapper.toRef(financialInstrumentDto.getCreditCard());
			//creditCardRef.setCvd(financialInstrumentDto.getCreditCard().getCvd());
			//creditCardRef.setZipCode(financialInstrumentDto.getCreditCard().getZipCode());
			creditCardRef.setCountryCode(member.getPerson().getAddress().getCountry());
			creditCardRef.setEmail(member.getPerson().getEmail());
			creditCardRef.setCustomerId(member.getUuid());
			
			PhoneNumberDto mobilePhone = member.getPerson().getPhoneNumbers().stream().filter(phone -> phone.getType() == PhoneNumberTypeEnumDto.mobile).findFirst().orElse(null);
			if (mobilePhone != null) {
				creditCardRef.setPhoneNumber(mobilePhone.getNumber());
			}
			
			// Validate credit card information
			validateCreditCard(creditCardRef);
			
			// Initialize financial instrument entity
			financialInstrument.setUuid(UUID.randomUUID().toString());
			financialInstrument.setBrandUuid(brand.getUuid());
			financialInstrument.setMemberUuid(member.getUuid());
			
			// Get CreditCard provider configuration
			PaymentProviderConfigurationEntry paymentProviderConfig = paymentProviderConfigurationPort.getPaymentProviderConfiguration(brand.getCode());

			// Verify credit card information with provider and get card type and issuer id
			financialInstrument = verifyCreditCard(paymentProviderConfig, financialInstrument, brand, creditCardRef);
			
			// Prepare credit card reference for vault add credit card
			creditCardRef.setIssuerId(financialInstrument.getCreditCard().getIssuerId());

			// Vault add credit card
			financialInstrument = registerCreditCard(paymentProviderConfig, financialInstrument, brand, creditCardRef);

			financialInstrument.setCreatedOn(Instant.now());
			financialInstrument.setCreatedBy(requestContext.getUsername());

			FinancialInstrument saved = financialInstrumentRepository.save(financialInstrument);
			
			return financialInstrumentMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", financialInstrumentDto != null ? financialInstrumentDto.getBrandUuid() : null, e);

			if (e instanceof FinancialInstrumentException) {
				throw (FinancialInstrumentException) e;
			}
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CREATION_FAILED, e);
		}
	}
	
	@Override
	@Transactional
	public FinancialInstrumentDto activate(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			MemberRef member = resolveMember(brand.getUuid(), memberUuid);

			FinancialInstrument entity = this.readByFinancialInstrumentUuid(brand.getUuid(), member.getUuid(), financialInstrumentUuid);
			entity.activate(requestContext.getUsername());
			financialInstrumentRepository.save(entity);
			
			return financialInstrumentMapper.toDto(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, financialInstrumentUuid={}", brandUuid, financialInstrumentUuid, e);

			if (e instanceof FinancialInstrumentException) {
				throw (FinancialInstrumentException) e;
			}
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public FinancialInstrumentDto deactivate(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			MemberRef member = resolveMember(brand.getUuid(), memberUuid);

			FinancialInstrument entity = this.readByFinancialInstrumentUuid(brand.getUuid(), member.getUuid(), financialInstrumentUuid);
			entity.deactivate(requestContext.getUsername());
			financialInstrumentRepository.save(entity);
			
			return financialInstrumentMapper.toDto(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, financialInstrumentUuid={}", brandUuid, financialInstrumentUuid, e);

			if (e instanceof FinancialInstrumentException) {
				throw (FinancialInstrumentException) e;
			}
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.DEACTIVATION_FAILED, e);
		}
	}
	
	@Override
	@Transactional
	public void delete(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			MemberRef member = resolveMember(brand.getUuid(), memberUuid);

			FinancialInstrument entity = this.readByFinancialInstrumentUuid(brand.getUuid(), member.getUuid(), financialInstrumentUuid);
			
			entity.delete(requestContext.getUsername());
			
			// TODO: Call CreditCard provider service to delete permanent token

			financialInstrumentRepository.save(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, financialInstrumentUuid={}, uuid={}", brandUuid, financialInstrumentUuid, financialInstrumentUuid, e);

			if (e instanceof FinancialInstrumentException) {
				throw (FinancialInstrumentException) e;
			}
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByBrandUuid(String brandUuid) throws FinancialInstrumentException {
		try {
			if (brandServicePort.brandDeleted(brandUuid)) {
				long deletedCount = financialInstrumentRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());

				logger.info("FinancialInstrument deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);
			}
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);

			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByMemberUuid(String brandUuid, String memberUuid) throws FinancialInstrumentException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			
			if (memberServicePort.memberDeleted(brand.getUuid(), memberUuid)) {
				long deletedCount = financialInstrumentRepository.deleteAllByMemberUuid(brandUuid, memberUuid, requestContext.getUsername());

				logger.info("FinancialInstrument deleted for member - brandUuid={} memberUuid={} deletedCount={} ", brandUuid, memberUuid, deletedCount);
			}
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);

			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.DELETE_FAILED, e);
		}
	}
	
	private FinancialInstrument readByFinancialInstrumentUuid(String brandUuid, String memberUuid, String financialInstrumentUuid) throws FinancialInstrumentException {
		Optional<FinancialInstrument> entity = financialInstrumentRepository.findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid, financialInstrumentUuid);
		if (entity.isEmpty()) {
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.FINANCIAL_INSTRUMENT_NOT_FOUND,
					"FinancialInstrument not found");
		}

		return entity.get();
	}
	
	private BrandRef resolveBrand(String brandUuid) throws FinancialInstrumentException {
		return brandServicePort.find(brandUuid)
				.orElseThrow(() -> new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.BRAND_NOT_FOUND,
				"Brand not found - brandUuid=" + brandUuid));
	}
	
	private MemberRef resolveMember(String brandUuid, String memberUuid) throws FinancialInstrumentException {
		return  memberServicePort.find(brandUuid, memberUuid)
				.orElseThrow(() -> new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.MEMBER_NOT_FOUND,
				"Member not found - brandUuid=" + brandUuid + " memberUuid=" + memberUuid));
	}
	
	private FinancialInstrument verifyCreditCard(
			PaymentProviderConfigurationEntry paymentProviderConfig, 
			FinancialInstrument financialInstrument, 
			BrandRef brand, 
			CreditCardRef creditCard) throws FinancialInstrumentException {
		try {	
			
			// Verify card with file credentials
			ReceiptRef receipt = paymentProviderPort.verify(paymentProviderConfig, creditCard);
			
			if (!receipt.isApproved()) {
				throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CARD_VERIFICATION_FAILED,
						"Card verification failed - providerResponseCode=" + receipt.getProviderResponseCode() + " message=" + receipt.getMessage());
			}
			
			if (!receipt.isAvsResultCode()) {
				throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CARD_VERIFICATION_AVS_FAILED,
						"Card verification failed - isAvsResultCode=" + receipt.isAvsResultCode() + " message=" + receipt.getMessage());
			}
			
			if (!receipt.isCvdResultCode()) {
				throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CARD_VERIFICATION_CVD_FAILED,
						"Card verification failed - isCvdResultCode=" + receipt.isCvdResultCode() + " message=" + receipt.getMessage());
			}

			// Set financial instrument credit card information based on verification result
			financialInstrument.getCreditCard().setIssuerId(receipt.getIssuerId());
			financialInstrument.getCreditCard().setCardType(receipt.getCardType());		
			
			return financialInstrument;
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", financialInstrument != null ? financialInstrument.getBrandUuid() : null, e);

			if (e instanceof FinancialInstrumentException) {
				throw (FinancialInstrumentException) e;
			}
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CARD_VERIFICATION_FAILED, e);
		}
	}
	
	private FinancialInstrument registerCreditCard(
						PaymentProviderConfigurationEntry paymentProviderConfig,
						FinancialInstrument financialInstrument, 
						BrandRef brandRef, 
						CreditCardRef creditCardRef) throws FinancialInstrumentException {
		try {

			ReceiptRef receipt = paymentProviderPort.register(paymentProviderConfig, creditCardRef);
			
			financialInstrument.getCreditCard().setCardNumber(financialInstrument.getCreditCard().getCardNumber().replaceAll("\\w(?=\\w{4})", "*"));
			financialInstrument.getCreditCard().setPermanentToken(receipt.getPermanentToken());

			return financialInstrument;
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", financialInstrument != null ? financialInstrument.getBrandUuid() : null, e);

			if (e instanceof FinancialInstrumentException) {
				throw (FinancialInstrumentException) e;
			}
			throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CARD_REGISTRATION_FAILED, e);
		}
	}
	
	private boolean validateCreditCard(CreditCardRef creditCardRef) throws FinancialInstrumentException {
		
		if (creditCardRef.getCardHolderName() == null || creditCardRef.getCardHolderName().isEmpty()) {
			 throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CARD_HOLDER_NAME_REQUIRED,
					 "Card holder name is required");
		}
		
		if (creditCardRef.getCardNumber() == null || creditCardRef.getCardNumber().isEmpty()) {
			 throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CARD_NUMBER_REQUIRED,
					 "Card number is required");
		}
		
		if (creditCardRef.getCvd() == null || creditCardRef.getCvd().isEmpty()) {
			 throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CARD_CVD_REQUIRED,
					 "CVD is required");
		}
		
		if (creditCardRef.getExpirationDate() == null || creditCardRef.getExpirationDate().isEmpty()) {
			 throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CARD_EXPIRY_DATE_REQUIRED,
					 "Card expiry date is required");
		}
		
		if (creditCardRef.getZipCode() == null || creditCardRef.getZipCode().isEmpty()) {
			 throw new FinancialInstrumentException(requestContext.getTrackingNumber(), FinancialInstrumentException.CARD_ZIPCODE_REQUIRED,
					 "Card zipcode is required");
		}
		
		return true;
	}

}