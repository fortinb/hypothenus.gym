package com.iso.hypo.domain.pricing;

import java.util.List;

import com.iso.hypo.domain.LocalizedString;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OneTimeFee {
	
	private String code;
	
	private Cost cost;
	
	private List<LocalizedString> description;
	
	public OneTimeFee() {
	}
	
	public OneTimeFee(String code, List<LocalizedString> description, Cost cost) {
		this.code = code;
		this.description = description;
		this.cost = cost;
	}
}
