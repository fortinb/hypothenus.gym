package com.iso.hypo.sale.domain.model;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.common.domain.model.finance.Currency;
import com.iso.hypo.finance.domain.model.Tax;
import com.iso.hypo.sale.domain.model.enumeration.OrderProcessingStateEnum;
import com.iso.hypo.sale.domain.model.enumeration.OrderStatusEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order extends BaseEntity  {

	private String id;
	
	private String uuid;
	
	private String brandUuid;
	
	private String memberUuid;
	
	private List<String> paymentsUuid;
	
	private Currency currency;
	
	private BillingDetail billingDetail;
	
	private String orderNumber;
	
	private OrderStatusEnum status;
	
	private OrderProcessingStateEnum processingState;
	
	private List<OrderItem> items;
	
	private ShippingDetail shippingDetail;
	
	private DiscountDetail discountDetail;
	
	private PaymentDetail paymentDetail;
	
	private Cost deposit;
	
	private Cost subTotal;
	
	private List<Tax> taxes;
	
	private Cost shippingTotal;
	
	private Cost discountTotal;
	
	private Cost total;
	
	private Instant submittedOn;

	private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final int ORDER_NUMBER_LENGTH = 6;
	private static final Random RANDOM = new Random();
	
	public Order() {
	}

	public Order(String uuid, String brandUuid, String memberUuid, BillingDetail billingDetail, String orderNumber,
			OrderStatusEnum status, OrderProcessingStateEnum processingState, List<OrderItem> items, ShippingDetail shippingDetail,
			DiscountDetail discountDetail, PaymentDetail paymentDetail, Cost deposit, Cost subTotal,
			List<Tax> taxes, Cost shippingTotal, Cost discountTotal, Cost total,
			Instant createdOn, Instant submittedOn) {
		this.uuid = uuid;
		this.brandUuid = brandUuid;
		this.memberUuid = memberUuid;
		this.billingDetail = billingDetail;
		this.orderNumber = orderNumber;
		this.status = status;
		this.processingState = processingState;
		this.status = status;
		this.items = items;
		this.shippingDetail = shippingDetail;
		this.discountDetail = discountDetail;
		this.paymentDetail = paymentDetail;
		this.deposit = deposit;
		this.subTotal = subTotal;
		this.taxes = taxes;
		this.shippingTotal = shippingTotal;
		this.discountTotal = discountTotal;
		this.total = total;
		this.createdOn = createdOn;
		this.submittedOn = submittedOn;
	}
	
	public void calculateCost() {
		this.subTotal = items.stream()
				.map(OrderItem::getItemTotal)
				.reduce(new Cost(0, currency), Cost::add);

		this.shippingTotal = shippingDetail != null ? shippingDetail.getShippingCost(new Cost(499, currency)) : new Cost(0, currency);
		
		this.discountTotal = discountDetail != null ? discountDetail.getDiscount(subTotal) : new Cost(0, currency);
		
		this.total = new Cost(subTotal.getAmount(), currency).add(shippingTotal).subtract(discountTotal);
		if (this.deposit != null) {
			this.total = this.total.subtract(deposit);
		}
		
		if (this.taxes != null && !taxes.isEmpty()) {
			Cost calculatedTaxes = taxes.stream()
					.map(tax -> tax.calculateTaxAmount(subTotal))
					.reduce(new Cost(0, currency), Cost::add);
			
			this.total.add(calculatedTaxes);
		}
	}
	
	/**
	 * Generates a structured, human-readable order number in the format:
	 *   ORD-{YYYYMMDD}-{BRAND_PREFIX}-{RANDOM_SUFFIX}
	 *
	 * Example: ORD-20260601-ACM-K7F3Q2
	 *
	 * - YYYYMMDD  : current UTC date
	 * - BRAND_PREFIX : first 3 chars of the brandUuid (upper-cased), giving loose brand affinity
	 * - RANDOM_SUFFIX: 6 random alphanumeric characters for uniqueness within the day
	 */
	public String generateOrderNumber(String prefix) {
		// Date part – UTC, e.g. "20260601"
		String datePart = DateTimeFormatter.ofPattern("yyyyMMdd")
				.withZone(ZoneOffset.UTC)
				.format(Instant.now());

		// Random alphanumeric suffix
		StringBuilder suffix = new StringBuilder(ORDER_NUMBER_LENGTH);
		for (int i = 0; i < ORDER_NUMBER_LENGTH; i++) {
			suffix.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
		}

		this.orderNumber = "ORD-" + prefix.toUpperCase(Locale.ROOT) + "-" + datePart + "-" + suffix.toString();
		return this.orderNumber;
	}
}
