package com.iso.hypo.admin.papi.dto.order;

import com.iso.hypo.admin.papi.dto.finance.CostDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscountDetailDto {

	private String couponCode;

	private Double rate;

	private CostDto amount;

	public DiscountDetailDto() {
	}
}
