package com.iso.hypo.admin.papi.dto.post;

import java.util.List;

import com.iso.hypo.admin.papi.dto.finance.CostDto;
import com.iso.hypo.admin.papi.dto.order.BillingDetailDto;
import com.iso.hypo.admin.papi.dto.order.DiscountDetailDto;
import com.iso.hypo.admin.papi.dto.order.OrderItemDto;
import com.iso.hypo.admin.papi.dto.order.PaymentDetailDto;
import com.iso.hypo.admin.papi.dto.order.ShippingDetailDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostOrderDto {

	@NotBlank
	private String brandUuid;

	@NotBlank
	private String memberUuid;
	
	private BillingDetailDto billingDetail;

	@NotEmpty
	private List<OrderItemDto> items;

	private ShippingDetailDto shippingDetail;

	private DiscountDetailDto discountDetail;

	private PaymentDetailDto paymentDetail;

	private CostDto deposit;

	public PostOrderDto() {
	}
}
