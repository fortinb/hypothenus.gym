package com.iso.hypo.sale.domain.model;

import com.iso.hypo.common.domain.model.finance.Cost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class DiscountDetail {

    private String couponCode;
    private Double rate; // e.g. "10%" or "15.5%"
    private Cost amount;
    
	public DiscountDetail() {
	}

	public DiscountDetail(String couponCode, Double rate, Cost amount) {
		this.couponCode = couponCode;
		this.rate = rate;
		this.amount = amount;
	}

	public Cost getDiscount(Cost subTotal) {
		if (rate != null) {
			return new Cost((int)(Math.round(subTotal.getAmount() * rate / 100.0)), subTotal.getCurrency());
		} else if (amount != null) {
			return amount;
		}
		return null;
	}
}
