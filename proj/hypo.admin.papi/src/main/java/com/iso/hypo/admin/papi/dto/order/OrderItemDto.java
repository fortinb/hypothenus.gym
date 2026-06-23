package com.iso.hypo.admin.papi.dto.order;

import com.iso.hypo.admin.papi.dto.finance.CostDto;

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
