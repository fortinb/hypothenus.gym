package com.iso.hypo.admin.papi.dto.pricing;

import java.util.List;

import com.iso.hypo.admin.papi.dto.LocalizedStringDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OneTimeFeeDto {
	
	private String code;
	
	private CostDto cost;
	
	private List<LocalizedStringDto> description;
	
	public OneTimeFeeDto() {
	}
	
	public OneTimeFeeDto(String code, List<LocalizedStringDto> description, CostDto cost) {
		this.code = code;
		this.description = description;
		this.cost = cost;
	}
}
