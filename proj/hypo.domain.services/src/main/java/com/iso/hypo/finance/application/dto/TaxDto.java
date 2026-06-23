package com.iso.hypo.finance.application.dto;

import java.util.List;

import com.iso.hypo.common.application.dto.finance.CostDto;
import com.iso.hypo.common.domain.model.LocalizedString;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaxDto {
	
    private List<LocalizedString> code;
    
    private List<LocalizedString> name;
    
    private String authority;
    
    private String jurisdiction;
    
    private String registrationNumber;
    
    private Double rate; 
    
    private CostDto taxableAmount;
    
    private CostDto taxAmount;
    
    private Boolean isIncludedInPrice;
    
	public TaxDto() {
	}
}
