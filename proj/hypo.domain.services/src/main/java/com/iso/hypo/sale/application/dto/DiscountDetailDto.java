package com.iso.hypo.sale.application.dto;

import com.iso.hypo.common.application.dto.finance.CostDto;

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
