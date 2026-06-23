package com.iso.hypo.sale.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.common.domain.model.finance.Currency;
import com.iso.hypo.common.infrastructure.persistence.entity.BaseDocument;
import com.iso.hypo.finance.domain.model.Tax;
import com.iso.hypo.sale.domain.model.BillingDetail;
import com.iso.hypo.sale.domain.model.DiscountDetail;
import com.iso.hypo.sale.domain.model.OrderItem;
import com.iso.hypo.sale.domain.model.PaymentDetail;
import com.iso.hypo.sale.domain.model.ShippingDetail;
import com.iso.hypo.sale.domain.model.enumeration.OrderProcessingStateEnum;
import com.iso.hypo.sale.domain.model.enumeration.OrderStatusEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("order")
public class OrderDocument extends BaseDocument {

	@Id
	private String id;

	@Indexed
	private String uuid;

	@Indexed
	private String brandUuid;

	@Indexed
	private String memberUuid;
	
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

	public OrderDocument() {
	}

	public OrderDocument(String uuid, String brandUuid, String memberUuid, BillingDetail billingDetail,
			String orderNumber, OrderStatusEnum status, List<OrderItem> items,
			ShippingDetail shippingDetail, DiscountDetail discountDetail,
			PaymentDetail paymentDetail, Cost deposit, Cost subTotal,
			List<Tax> taxes, Cost shippingTotal, Cost discountTotal, Cost total,
			Instant createdOn, Instant submittedOn) {
		this.uuid = uuid;
		this.brandUuid = brandUuid;
		this.memberUuid = memberUuid;
		this.billingDetail = billingDetail;
		this.orderNumber = orderNumber;
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
}
