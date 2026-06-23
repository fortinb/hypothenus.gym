package com.iso.hypo.admin.papi.dto.model;

import java.time.Instant;
import java.util.List;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.finance.CostDto;
import com.iso.hypo.admin.papi.dto.finance.CurrencyDto;
import com.iso.hypo.admin.papi.dto.finance.TaxDto;
import com.iso.hypo.admin.papi.dto.order.BillingDetailDto;
import com.iso.hypo.admin.papi.dto.order.DiscountDetailDto;
import com.iso.hypo.admin.papi.dto.order.OrderItemDto;
import com.iso.hypo.admin.papi.dto.order.PaymentDetailDto;
import com.iso.hypo.admin.papi.dto.order.ShippingDetailDto;
import com.iso.hypo.common.application.dto.enumeration.OrderStatusEnumDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDto extends BaseDto {

	private String uuid;

	private String brandUuid;

	private String memberUuid;
	
	private List <String> paymentsUuid;

	private String orderNumber;
	
	private CurrencyDto currency;

	private OrderStatusEnumDto status;

	private BillingDetailDto billingDetail;

	private List<OrderItemDto> items;

	private ShippingDetailDto shippingDetail;

	private DiscountDetailDto discountDetail;

	private PaymentDetailDto paymentDetail;

	private CostDto deposit;

	private CostDto subTotal;

	private CostDto shippingTotal;

	private CostDto discountTotal;
	
	private List<TaxDto> taxes;

	private CostDto total;

	private Instant submittedOn;

	public OrderDto() {
	}
}
