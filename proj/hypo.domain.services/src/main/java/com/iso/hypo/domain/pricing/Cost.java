package com.iso.hypo.domain.pricing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cost {
	
	private int amount;
	
	private Currency currency;
	
	public Cost() {
	}
	
	public Cost(int amount, Currency currency) {
		super();
		this.amount = amount;
		this.currency = currency;
	}
}
