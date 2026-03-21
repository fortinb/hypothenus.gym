package com.iso.hypo.admin.papi.dto.pricing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CostDto {
	
	private int amount;
	
	private CurrencyDto currency;
	
	public CostDto() {
	}
	
	public CostDto(int amount, CurrencyDto currency) {
		super();
		this.amount = amount;
		this.currency = currency;
	}
}
