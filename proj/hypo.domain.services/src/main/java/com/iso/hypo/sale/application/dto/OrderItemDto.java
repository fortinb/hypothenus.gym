package com.iso.hypo.sale.application.dto;

import com.iso.hypo.common.application.dto.finance.CostDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {

	private MembershipPlanDto membershipPlan;

	private CostDto unitPrice;

	private int quantity;

	private CostDto itemTotal;

	public OrderItemDto() {
	}
}
