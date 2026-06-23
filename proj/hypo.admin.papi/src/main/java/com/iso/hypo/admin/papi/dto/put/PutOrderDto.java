package com.iso.hypo.admin.papi.dto.put;

import java.time.Instant;
import java.util.List;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.finance.CostDto;
import com.iso.hypo.admin.papi.dto.order.BillingDetailDto;
import com.iso.hypo.admin.papi.dto.order.DiscountDetailDto;
import com.iso.hypo.admin.papi.dto.order.OrderItemDto;
import com.iso.hypo.admin.papi.dto.order.PaymentDetailDto;
import com.iso.hypo.admin.papi.dto.order.ShippingDetailDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutOrderDto extends BaseDto {

	private String uuid;

	private String brandUuid;

	private String memberUuid;

	private String orderNumber;

	private BillingDetailDto billingDetail;

	private List<OrderItemDto> items;

	private ShippingDetailDto shippingDetail;

	private DiscountDetailDto discountDetail;

	private PaymentDetailDto paymentDetail;

	private CostDto deposit;

	private Instant submittedOn;

	public PutOrderDto() {
	}
}
