package com.iso.hypo.gym.admin.papi.dto.pricing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CostDto {
	
	private Integer cost;
	
	private CurrencyDto currency;
	
	public CostDto() {
	}
	
	public CostDto(Integer cost, CurrencyDto currency) {
		super();
		this.cost = cost;
		this.currency = currency;
	}
}
