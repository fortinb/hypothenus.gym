package com.iso.hypo.sale.application.usecase.impl;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.brand.infrastructure.persistence.repository.BrandMongoRepository;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.enumeration.MembershipPlanPeriodEnumDto;
import com.iso.hypo.common.application.dto.enumeration.PaymentMethodEnumDto;
import com.iso.hypo.common.application.dto.enumeration.PaymentStatusEnumDto;
import com.iso.hypo.common.application.mapper.AddressDtoMapper;
import com.iso.hypo.common.application.port.TaxConfigurationEntry;
import com.iso.hypo.common.application.port.TaxConfigurationPort;
import com.iso.hypo.common.domain.model.Message;
import com.iso.hypo.common.domain.model.enumeration.MessageSeverityEnum;
import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.finance.domain.model.Tax;
import com.iso.hypo.sale.application.dto.OrderDto;
import com.iso.hypo.sale.application.exception.OrderException;
import com.iso.hypo.sale.application.mapper.OrderDtoMapper;
import com.iso.hypo.sale.application.port.BrandServicePort;
import com.iso.hypo.sale.application.port.MemberServicePort;
import com.iso.hypo.sale.application.port.MembershipPlanServicePort;
import com.iso.hypo.sale.application.port.MembershipServicePort;
import com.iso.hypo.sale.application.port.PaymentServicePort;
import com.iso.hypo.sale.application.port.dto.BrandRef;
import com.iso.hypo.sale.application.port.dto.MemberRef;
import com.iso.hypo.sale.application.port.dto.MembershipPlanRef;
import com.iso.hypo.sale.application.port.dto.MembershipRef;
import com.iso.hypo.sale.application.port.dto.PaymentRef;
import com.iso.hypo.sale.application.port.mapper.MembershipPlanRefMapper;
import com.iso.hypo.sale.application.usecase.OrderService;
import com.iso.hypo.sale.domain.model.BillingDetail;
import com.iso.hypo.sale.domain.model.Order;
import com.iso.hypo.sale.domain.model.OrderItem;
import com.iso.hypo.sale.domain.model.PaymentDetail;
import com.iso.hypo.sale.domain.model.ShippingDetail;
import com.iso.hypo.sale.domain.model.enumeration.OrderProcessingStateEnum;
import com.iso.hypo.sale.domain.model.enumeration.OrderStatusEnum;
import com.iso.hypo.sale.domain.model.enumeration.PaymentMethodEnum;
import com.iso.hypo.sale.domain.model.enumeration.ShippingMethodEnum;
import com.iso.hypo.sale.domain.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {

	private final BrandServicePort brandServicePort;

	private final OrderDtoMapper orderMapper;

	private final OrderRepository orderRepository;

	private final MembershipPlanServicePort membershipPlanServicePort;

	private final MembershipServicePort membershipServicePort;

	private final MemberServicePort memberServicePort;

	private final PaymentServicePort paymentServicePort;

	private final TaxConfigurationPort taxConfigurationPort;

	private final AddressDtoMapper addressMapper;

	private final ModelMapper modelMapper;

	private final MembershipPlanRefMapper membershipPlanRefMapper;

	private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

	private final RequestContext requestContext;
	
	@Value("${app.payment.provider.test:true}")
	private boolean paymentProviderTest;

	public OrderServiceImpl(OrderRepository orderRepository, BrandServicePort brandServicePort,
			MembershipPlanServicePort membershipPlanServicePort, MembershipServicePort membershipServicePort,
			MemberServicePort memberServicePort, PaymentServicePort paymentServicePort,
			TaxConfigurationPort taxConfigurationPort, AddressDtoMapper addressMapper, OrderDtoMapper orderMapper,
			ModelMapper modelMapper, MembershipPlanRefMapper membershipPlanRefMapper, RequestContext requestContext,
			BrandMongoRepository brandMongoRepository) {
		this.orderRepository = orderRepository;
		this.brandServicePort = brandServicePort;
		this.membershipPlanServicePort = membershipPlanServicePort;
		this.membershipServicePort = membershipServicePort;
		this.memberServicePort = memberServicePort;
		this.paymentServicePort = paymentServicePort;
		this.taxConfigurationPort = taxConfigurationPort;
		this.addressMapper = addressMapper;
		this.orderMapper = orderMapper;
		this.modelMapper = modelMapper;
		this.membershipPlanRefMapper = membershipPlanRefMapper;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public OrderDto create(OrderDto orderDto) throws OrderException {
		try {
			Assert.notNull(orderDto, "orderDto must not be null");

			Order order = orderMapper.toEntity(orderDto);

			BrandRef brand = resolveBrand(orderDto.getBrandUuid());
			MemberRef member = resolveMember(brand.getUuid(), orderDto.getMemberUuid());

			// Initialize order
			initializeOrder(order, brand, member);

			// Validate and prepare order items
			validateAndPrepareOrderItems(order, brand, member);

			// Calculate Cost
			order.calculateCost();

			order.setUuid(UUID.randomUUID().toString());
			order.setMemberUuid(member.getUuid());
			order.setStatus(OrderStatusEnum.created);
			order.setCreatedOn(Instant.now());
			order.setCreatedBy(requestContext.getUsername());

			Order saved = orderRepository.save(order);

			return orderMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", orderDto != null ? orderDto.getBrandUuid() : null, e);

			if (e instanceof OrderException) {
				throw (OrderException) e;
			}
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.CREATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public OrderDto update(OrderDto orderDto) throws OrderException {
		try {
			Assert.notNull(orderDto, "orderDto must not be null");

			BrandRef brand = resolveBrand(orderDto.getBrandUuid());
			MemberRef member = resolveMember(brand.getUuid(), orderDto.getMemberUuid());

			Order order = orderMapper.toEntity(orderDto);
			Order oldOrder = this.readByOrderUuid(brand.getUuid(), member.getUuid(), orderDto.getUuid());

			// Map Order Request to existing order. Only non-null fields in the request will
			// be mapped to existing order
			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(true).setCollectionsMergeEnabled(false);
			mapper = orderMapper.initOrderMappings(mapper);
			mapper.map(order, oldOrder);

			// Validate and prepare order - resolve discount, resolve shipping detail,
			// resolve payment detail from request, etc.
			validateAndPrepareOrder(oldOrder, brand, member);

			// Validate and prepare order items
			validateAndPrepareOrderItems(oldOrder, brand, member);

			// Calculate Cost
			oldOrder.calculateCost();

			oldOrder.setModifiedOn(Instant.now());
			oldOrder.setModifiedBy(requestContext.getUsername());

			Order saved = orderRepository.save(oldOrder);
			return orderMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - orderUuid={}", orderDto.getUuid(), e);
			if (e instanceof OrderException) {
				throw (OrderException) e;
			}
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public OrderDto patch(OrderDto orderDto) throws OrderException {
		try {
			Assert.notNull(orderDto, "orderDto must not be null");

			BrandRef brand = resolveBrand(orderDto.getBrandUuid());
			MemberRef member = resolveMember(brand.getUuid(), orderDto.getMemberUuid());

			Order order = orderMapper.toEntity(orderDto);
			Order oldOrder = this.readByOrderUuid(brand.getUuid(), member.getUuid(), orderDto.getUuid());

			// Map items from request to existing order.
			oldOrder.setItems(order.getItems());

			// Validate and prepare order items
			validateAndPrepareOrderItems(oldOrder, brand, member);

			// Calculate Cost
			oldOrder.calculateCost();

			oldOrder.setModifiedOn(Instant.now());
			oldOrder.setModifiedBy(requestContext.getUsername());

			Order saved = orderRepository.save(oldOrder);
			return orderMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - orderUuid={}", orderDto.getUuid(), e);
			if (e instanceof OrderException) {
				throw (OrderException) e;
			}
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public OrderDto submit(String brandUuid, String memberUuid, String orderUuid, OrderDto orderDto)
			throws OrderException {
		try {
			Assert.notNull(orderDto, "orderDto must not be null");

			BrandRef brand = resolveBrand(orderDto.getBrandUuid());
			MemberRef member = resolveMember(brand.getUuid(), orderDto.getMemberUuid());

			// Read order
			Order order = this.readByOrderUuid(brand.getUuid(), member.getUuid(), orderDto.getUuid());

			// Map Order Request to existing order before submit.
			order = prepareOrderForSubmit(orderDto, order);
			
			// Validate order 
			validateOrderForSubmit(order);
			 
			// Update order status to submitted if the order is in idle status. 
			if (order.getProcessingState() == OrderProcessingStateEnum.idle) {
				order.setProcessingState(OrderProcessingStateEnum.takePayment);
				
				order.setStatus(OrderStatusEnum.submitted);
				order.setSubmittedOn(Instant.now());
				
				order = orderRepository.save(order);
			}

			// Take payment
			if (order.getProcessingState() == OrderProcessingStateEnum.takePayment) {		
				PaymentRef paymentResult = takePayment(brand, member, order);
				
				// Initialize PaymentUuid for record keeping, regardless of payment success or failure.
				if (order.getPaymentsUuid() == null) {
					order.setPaymentsUuid(List.of(paymentResult.getUuid()));
				} else {
					order.getPaymentsUuid().add(paymentResult.getUuid());
				}
				
				if (paymentResult.getPaymentStatus() == PaymentStatusEnumDto.completed) {
					// Payment successful
					order.setProcessingState(OrderProcessingStateEnum.createMembership);
				} else {
					// Payment failed, update order status to failed.
					order.setStatus(OrderStatusEnum.paymentFailed);
				}
				
				// Update order with payment result
				order = orderRepository.save(order);
			}

			// If payment is successful create membership
			if (order.getProcessingState() == OrderProcessingStateEnum.createMembership) {	
				order = createMembership(brand, member, order);
				
				// Order is paid and membership are created successfully.
				order.setProcessingState(OrderProcessingStateEnum.completed);
				order.setStatus(OrderStatusEnum.completed);
			} 

			order.setModifiedOn(Instant.now());
			order.setModifiedBy(requestContext.getUsername());
			
			orderRepository.save(order);

			return orderMapper.toDto(order);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, orderUuid={}", brandUuid, orderUuid, e);

			if (e instanceof OrderException) {
				throw (OrderException) e;
			}
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.ORDER_SUBMIT_FAILED, e);
		}
	}

	@Override
	@Transactional
	public OrderDto cancel(String brandUuid, String memberUuid, String orderUuid) throws OrderException {
		try {

			BrandRef brand = resolveBrand(brandUuid);
			MemberRef member = resolveMember(brand.getUuid(), memberUuid);

			Order order = this.readByOrderUuid(brand.getUuid(), member.getUuid(), orderUuid);

			order.setStatus(OrderStatusEnum.cancelled);
			order.setModifiedBy(requestContext.getUsername());
			order.setModifiedOn(Instant.now());

			orderRepository.save(order);

			return orderMapper.toDto(order);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, orderUuid={}", brandUuid, orderUuid, e);

			if (e instanceof OrderException) {
				throw (OrderException) e;
			}
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public void delete(String brandUuid, String memberUuid, String orderUuid) throws OrderException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			MemberRef member = resolveMember(brand.getUuid(), memberUuid);

			Order order = this.readByOrderUuid(brand.getUuid(), member.getUuid(), orderUuid);
			order.delete(requestContext.getUsername());

			orderRepository.save(order);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, orderUuid={}", brandUuid, orderUuid, e);

			if (e instanceof OrderException) {
				throw (OrderException) e;
			}
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByBrandUuid(String brandUuid) throws OrderException {
		try {
			if (brandServicePort.brandDeleted(brandUuid)) {
				long deletedCount = orderRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());

				logger.info("Orders deleted for brand - brandUuid={} deletedCount={}", brandUuid, deletedCount);
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);

			throw new OrderException(requestContext.getTrackingNumber(), OrderException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByMemberUuid(String brandUuid, String memberUuid) throws OrderException {
		try {
			BrandRef brand = resolveBrand(brandUuid);
			
			if (memberServicePort.memberDeleted(brand.getUuid(), memberUuid)) {
				long deletedCount = orderRepository.deleteAllByMemberUuid(brand.getUuid(), memberUuid, requestContext.getUsername());

				logger.info("Orders deleted for member - brandUuid={} memberUuid={} deletedCount={}", brandUuid, memberUuid,
						deletedCount);
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);

			throw new OrderException(requestContext.getTrackingNumber(), OrderException.DELETE_FAILED, e);
		}
	}

	private Order readByOrderUuid(String brandUuid, String memberUuid, String orderUuid) throws OrderException {
		Optional<Order> entity = orderRepository.findByBrandUuidAndMemberUuidAndUuidAndDeletedIsFalse(brandUuid,
				memberUuid, orderUuid);
		if (entity.isEmpty()) {
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.ORDER_NOT_FOUND,
					"Order not found");
		}

		return entity.get();
	}

	private BrandRef resolveBrand(String brandUuid) throws OrderException {
		return brandServicePort.find(brandUuid).orElseThrow(() -> new OrderException(requestContext.getTrackingNumber(),
				OrderException.BRAND_NOT_FOUND, "Brand not found - brandUuid=" + brandUuid));
	}

	private MemberRef resolveMember(String brandUuid, String memberUuid) throws OrderException {
		return memberServicePort.find(brandUuid, memberUuid).orElseThrow(
				() -> new OrderException(requestContext.getTrackingNumber(), OrderException.MEMBER_NOT_FOUND,
						"Member not found - brandUuid=" + brandUuid + " memberUuid=" + memberUuid));
	}

	private Order initializeOrder(Order order, BrandRef brand, MemberRef member) throws OrderException {
		try {
			// Initialize order number
			String brandPrefix = Optional.ofNullable(brand.getCode()).map(code -> code.replace("-", ""))
					.filter(s -> s.length() >= 3).map(s -> s.substring(0, 3).toUpperCase(Locale.ROOT)).orElse("HYP");
			order.generateOrderNumber(brandPrefix);

			// Initialize order currency from brand
			order.setCurrency(brand.getCurrency());

			// Initialize billing detail from member
			if (order.getBillingDetail() == null) {
				BillingDetail billingDetail = new BillingDetail();
				billingDetail.setAddress(addressMapper.toEntity(member.getPerson().getAddress()));
				billingDetail.setEmail(member.getPerson().getEmail());
				billingDetail.setName(member.getPerson().getFullName());

				order.setBillingDetail(billingDetail);
			}

			// Initialize shipping detail to default value - empty
			if (order.getShippingDetail() == null) {
				ShippingDetail shippingDetail = new ShippingDetail();
				shippingDetail.setAddress(null);
				shippingDetail.setShippingMethod(ShippingMethodEnum.email);

				order.setShippingDetail(shippingDetail);
			}

			// Initialize payment detail to default value - empty
			if (order.getPaymentDetail() == null) {
				PaymentDetail paymentDetail = new PaymentDetail();
				paymentDetail.setPaymentMethod(PaymentMethodEnum.credit);

				if (member.getPreferredFinancialInstrumenUuid() != null) {
					paymentDetail.setFinancialInstrumentUuid(member.getPreferredFinancialInstrumenUuid());
				}

				order.setPaymentDetail(paymentDetail);
			}

			// Initialize taxes to country/state value
			String country = order.getBillingDetail().getAddress().getCountry();
			String state = order.getBillingDetail().getAddress().getState();
			order.setTaxes(resolveTaxes(taxConfigurationPort, country, state));

			order.setStatus(OrderStatusEnum.created);
			order.setProcessingState(OrderProcessingStateEnum.idle);
			
			return order;
		} catch (Exception e) {
			logger.error("Error - orderdUuid={}", order != null ? order.getUuid() : null, e);
			if (e instanceof OrderException) {
				throw (OrderException) e;
			}
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.UPDATE_FAILED, e);
		}
	}

	private void validateAndPrepareOrderItems(Order order, BrandRef brand, MemberRef member) throws OrderException {

		// Validate/Initialize order items
		for (OrderItem item : order.getItems()) {
			MembershipPlanRef membershipPlan = membershipPlanServicePort
					.find(order.getBrandUuid(), item.getMembershipPlan().getUuid())
					.orElseThrow(() -> new OrderException(requestContext.getTrackingNumber(),
							OrderException.MEMBERSHIP_PLAN_NOT_FOUND,
							"MembershipPlan not found - membershipPlanUuid=" + item.getMembershipPlan().getUuid()));

			// Initialize membership plan
			item.setMembershipPlan(membershipPlanRefMapper.toEntity(membershipPlan));

			// Validate trial membership plan
			if (item.getMembershipPlan().getPeriod() == MembershipPlanPeriodEnumDto.trial) {

				// Trial membership plan are only available for new member, validate with
				// membership service
				if (!membershipServicePort.isNewMember(brand.getUuid(), member.getUuid())) {
					Message message = new Message();
					message.setCode(OrderException.TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER);
					message.setDescription("Not a new member");
					message.setSeverity(MessageSeverityEnum.warning);
					order.setMessages(List.of(message));

					throw new OrderException(requestContext.getTrackingNumber(),
							OrderException.TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER,
							"Trial membership plan is only available for new member - memberUuid=" + member.getUuid()
									+ " membershipPlanUuid=" + item.getMembershipPlan().getUuid(),
							orderMapper.toDto(order));
				}

				// Additionally, validate that member does not have existing membership with the
				// same trial membership plan.
				Optional<MembershipRef> membership = membershipServicePort.findByMembershipPlanUuid(brand.getUuid(),
						member.getUuid(), item.getMembershipPlan().getUuid());
				if (membership.isPresent()) {
					Message message = new Message();
					message.setCode(OrderException.TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER);
					message.setDescription("Already have existing membership with the same trial membership plan");
					message.setSeverity(MessageSeverityEnum.warning);
					order.setMessages(List.of(message));

					throw new OrderException(requestContext.getTrackingNumber(),
							OrderException.TRIAL_MEMBERSHIP_PLAN_ONLY_FOR_NEW_MEMBER,
							"Already have existing membership with the same trial membership plan - memberUuid="
									+ member.getUuid() + " membershipPlanUuid=" + item.getMembershipPlan().getUuid(),
							orderMapper.toDto(order));
				}

				// Trial membership plan can only have quantity of 1, regardless of the value
				// sent by client.
				item.setQuantity(1);
			}

			// Calculate cost for each order item
			item.CalculateCost();
		}
	}

	private Order validateAndPrepareOrder(Order order, BrandRef brand, MemberRef member) throws OrderException {
		try {
			// Verify Shipping details
			// Verify discount code and resolve discount 
			// Verify gift card code and resolve gift card

			return order;
		} catch (Exception e) {
			logger.error("Error - orderdUuid={}", order != null ? order.getUuid() : null, e);
			if (e instanceof OrderException) {
				throw (OrderException) e;
			}
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.UPDATE_FAILED, e);
		}
	}

	private boolean validateOrderForSubmit(Order order) throws OrderException {

		// The received orderDto.Status is expected to be "submitted".
		if (order.getStatus() == OrderStatusEnum.cancelled) {
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.ORDER_CANCELLED,
					"Invalid order status - status=" + order.getStatus());
		}

		// Validate that order is in created or failed status before submitting the order.
		if (order.getStatus() == OrderStatusEnum.completed) {
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.ORDER_ALREADY_PROCESSED,
					"Order already processed - current status=" + order.getStatus());
		}

		if (order.getBillingDetail() == null) {
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.MISSING_BILLING_DETAIL,
					"Missing billing detail in request");
		}

		if (order.getBillingDetail().getEmail() == null || order.getBillingDetail().getEmail().isEmpty()) {
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.MISSING_BILLING_DETAIL,
					"Missing billing detail in request - email required");
		}

		if (order.getBillingDetail().getName() == null || order.getBillingDetail().getName().isEmpty()) {
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.MISSING_BILLING_DETAIL,
					"Missing billing detail in request - name required");
		}

		if (order.getPaymentDetail() == null) {
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.MISSING_PAYMENT_DETAIL,
					"Missing payment detail in request");
		}

		if (order.getPaymentDetail().getFinancialInstrumentUuid() == null) {
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.MISSING_PAYMENT_DETAIL,
					"Missing payment detail in request - financial instrument required");
		}

		if (order.getPaymentDetail().getFinancialInstrumentUuid().isEmpty()) {
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.MISSING_PAYMENT_DETAIL,
					"Missing payment detail in request - financial instrument required");
		}

		return true;
	}
	
	private Order prepareOrderForSubmit(OrderDto orderDto, Order order) {
		
		// Update Billing Detail,
		if (orderDto.getBillingDetail() != null) {
			if (orderDto.getBillingDetail().getAddress() != null) {
				order.getBillingDetail().setAddress(addressMapper.toEntity(orderDto.getBillingDetail().getAddress()));
			}
			
			order.getBillingDetail().setEmail(orderDto.getBillingDetail().getEmail());
			order.getBillingDetail().setName(orderDto.getBillingDetail().getName());
		}

		// Update Shipping Detail 
		if (orderDto.getShippingDetail() != null ) {
			if (orderDto.getShippingDetail().getAddress() != null) {
				order.getShippingDetail().setAddress(addressMapper.toEntity(orderDto.getShippingDetail().getAddress()));
			}
			
			order.getShippingDetail().setShippingMethod(modelMapper.map(orderDto.getShippingDetail().getShippingMethod(), ShippingMethodEnum.class));
		}
		
		// Update Payment Detail from request before submitting the order
		if (orderDto.getPaymentDetail() != null) {
			order.getPaymentDetail().setFinancialInstrumentUuid(orderDto.getPaymentDetail().getFinancialInstrumentUuid());
			order.getPaymentDetail().setPaymentMethod(modelMapper.map(orderDto.getPaymentDetail().getPaymentMethod(), PaymentMethodEnum.class));
		}
		return order;
	}

	private Order createMembership(BrandRef brand, MemberRef member, Order order) throws OrderException {
		List<MembershipRef> membershipsRef = new java.util.ArrayList<>();
		
		// Prepare membership to be created
		for (OrderItem orderItem : order.getItems()) {
			for (int i = 0; i < orderItem.getQuantity(); i++) {
				MembershipRef membershipRef = new MembershipRef();
				membershipRef.setBrandUuid(brand.getUuid());
				membershipRef.setMemberUuid(member.getUuid());
				membershipRef.setOrderUuid(order.getUuid());
				membershipRef.setMembershipPlan(membershipPlanRefMapper.toRef(orderItem.getMembershipPlan()));
				
				membershipsRef.add(membershipRef);
			}
		}
		
		// Create membership via membership service
		membershipsRef = membershipServicePort.create(membershipsRef);
		if (membershipsRef == null) {
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.MEMBERSHIP_CREATION_FAILED,
					"Memberships creation failed for order - " + order.getUuid(),
					orderMapper.toDto(order));
		}
		return order;
	}

	private PaymentRef takePayment(BrandRef brand, MemberRef member, Order order) throws Exception {
		try {
			Optional<PaymentRef> paymentResult = Optional.empty();
			
			// Take payment via payment service
			PaymentRef paymentRef = new PaymentRef();
			paymentRef.setBrandUuid(brand.getUuid());
			paymentRef.setMemberUuid(member.getUuid());
			paymentRef.setOrderUuid(order.getUuid());
			paymentRef.setOrderNumber(order.getOrderNumber());
			paymentRef.setPaymentMethod(modelMapper.map(order.getPaymentDetail().getPaymentMethod(), PaymentMethodEnumDto.class));
			paymentRef.setFinancialInstrumentUuid(order.getPaymentDetail().getFinancialInstrumentUuid());
			
			// For payment provider test, override the purchase amount with a fixed value to make sure the payment result is deterministic for testing purpose.
			if (paymentProviderTest) {
				paymentRef.setPurchaseAmount(new Cost(1001, order.getCurrency()));
			} else {
				paymentRef.setPurchaseAmount(order.getTotal());
			}
			
			paymentResult = paymentServicePort.purchase(paymentRef);

			if (paymentResult.isEmpty()) {
				throw new OrderException(requestContext.getTrackingNumber(), OrderException.ORDER_PAYMENT_FAILED,
						"Payment failed");
			}
			
			return paymentResult.get();
		} catch (Exception e) {
			logger.error("Payment Error - brandUuid={}, orderUuid={}", order.getBrandUuid(), order.getUuid(), e);
			throw new OrderException(requestContext.getTrackingNumber(), OrderException.ORDER_PAYMENT_FAILED, e);
		}
	}
	
	private List<Tax> resolveTaxes(TaxConfigurationPort taxConfigurationPort, String country, String state) {
		if (taxConfigurationPort == null || country == null || country.isEmpty() || state == null || state.isEmpty()) {
			return null;
		}

		List<TaxConfigurationEntry> entries = taxConfigurationPort.getTaxConfigurations(country, state);
		if (entries == null || entries.isEmpty()) {
			return null;
		}

		return entries.stream().map(entry -> modelMapper.map(entry, Tax.class)).collect(Collectors.toList());
	}

}