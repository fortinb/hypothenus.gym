package com.iso.hypo.sale.application.dto;

import java.time.Instant;
import java.util.List;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.application.dto.enumeration.OrderStatusEnumDto;
import com.iso.hypo.common.application.dto.finance.CostDto;
import com.iso.hypo.common.application.dto.finance.CurrencyDto;
import com.iso.hypo.finance.application.dto.TaxDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderDto extends BaseEntityDto {

	private String uuid;

	private String brandUuid;

	private String memberUuid;
	
	private List<String> paymentsUuid;
	
	private CurrencyDto currency;

	private BillingDetailDto billingDetail;

	private String orderNumber;

	private OrderStatusEnumDto status;

	private List<OrderItemDto> items;

	private ShippingDetailDto shippingDetail;

	private DiscountDetailDto discountDetail;

	private PaymentDetailDto paymentDetail;

	private CostDto deposit;

	private CostDto subTotal;

	private List<TaxDto> taxes;

	private CostDto shippingTotal;

	private CostDto discountTotal;

	private CostDto total;

	private Instant submittedOn;

	public OrderDto() {
	}
}
