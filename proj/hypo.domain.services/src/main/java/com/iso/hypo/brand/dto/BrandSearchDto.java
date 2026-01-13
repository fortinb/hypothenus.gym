package com.iso.hypo.brand.dto;

import com.iso.hypo.common.domain.location.Address;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandSearchDto {
	
	private String brandId;

	private String name;
	
	private String email;
	
	private Address address;
	
	private boolean isActive;
}
