package com.isoceles.hypothenus.gym.domain.model.pricing;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Currency {
	
	private String name;
	
	private String code;
	
	private String symbol;
	
	public Currency() {
	}
	
	public Currency(String name, String code, String symbol) {
		this.name = name;
		this.code = code;
		this.symbol = symbol;
	}
}
