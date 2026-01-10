package com.iso.hypo.gym.domain.model.pricing;

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
