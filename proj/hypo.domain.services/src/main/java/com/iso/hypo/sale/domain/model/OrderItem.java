package com.iso.hypo.sale.domain.model;

import com.iso.hypo.common.domain.model.finance.Cost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class OrderItem {

    private MembershipPlan membershipPlan;
    private Cost unitPrice;
    private int quantity;
    private Cost itemTotal;

	public OrderItem() {
	}

	public OrderItem(MembershipPlan membershipPlan, Cost unitPrice, Integer quantity, Cost itemTotal) {
		this.membershipPlan = membershipPlan;
		this.unitPrice = unitPrice;
		this.quantity = quantity;
		this.itemTotal = itemTotal;
	}
	
	public void CalculateCost() {
		unitPrice = membershipPlan.getPrice();
		
		// For membership plans, default quantity is 1
		if (quantity == 0) {
			quantity = 1;
		}
		
		this.itemTotal = new Cost(unitPrice.getAmount() * quantity, membershipPlan.getPrice().getCurrency());
	}
}
