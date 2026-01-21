package com.iso.hypo.model.domain.pricing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cost {
	
	private Integer cost;
	
	private Currency currency;
	
	public Cost() {
	}
	
	public Cost(Integer cost, Currency currency) {
		super();
		this.cost = cost;
		this.currency = currency;
	}
}
