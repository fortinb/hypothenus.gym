package com.iso.hypo.admin.papi.dto.order;

import com.iso.hypo.admin.papi.dto.location.AddressDto;

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
