package com.iso.hypo.sale.application.dto;

import com.iso.hypo.common.application.dto.location.AddressDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BillingDetailDto {

	private AddressDto address;

	private String email;

	private String name;

	public BillingDetailDto() {
	}
}
