package com.iso.hypo.finance.domain.model;

import java.util.List;

import com.iso.hypo.common.domain.model.LocalizedString;
import com.iso.hypo.common.domain.model.finance.Cost;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tax {
	
    private List<LocalizedString> code;
    private List<LocalizedString> name;
    private String authority;
    private String jurisdiction;
    private String registrationNumber;
    private Double rate; // decimal rate, e.g. 0.05 for 5%
    private Cost taxableAmount;
    private Cost taxAmount;
    private Boolean isIncludedInPrice;
    
	public Tax() {
	}

	public Tax(List<LocalizedString> code, List<LocalizedString> name, String authority, String jurisdiction, String registrationNumber,
			Double rate, Cost taxableAmount, Cost taxAmount, Boolean isIncludedInPrice) {
		this.code = code;
		this.name = name;
		this.authority = authority;
		this.jurisdiction = jurisdiction;
		this.registrationNumber = registrationNumber;
		this.rate = rate;
		this.taxableAmount = taxableAmount;
		this.taxAmount = taxAmount;
		this.isIncludedInPrice = isIncludedInPrice;
	}

	public Cost calculateTaxAmount(Cost taxableAmount) {
		this.taxableAmount = taxableAmount;
		if (taxableAmount == null || rate == null) {
			return null;
		}
		
		if (isIncludedInPrice)  {
			// Tax = totalAmount × rate / (1 + rate)
			long calculatedTax = Math.round((taxableAmount.getAmount() * rate / (1 + rate)));
			this.taxAmount = new Cost((int) calculatedTax, taxableAmount.getCurrency());
			return this.taxAmount;
		}
		
		long calculatedTax = Math.round(taxableAmount.getAmount() * rate);
		this.taxAmount = new Cost((int) calculatedTax, taxableAmount.getCurrency());
		return this.taxAmount;
	}
}
