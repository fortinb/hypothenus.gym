package com.iso.hypo.gym.admin.papi.dto.pricing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurrencyDto {
	
	private String name;
	
	private String code;
	
	private String symbol;
	
	public CurrencyDto() {
	}
	
	public CurrencyDto(String name, String code, String symbol) {
		this.name = name;
		this.code = code;
		this.symbol = symbol;
	}
}
