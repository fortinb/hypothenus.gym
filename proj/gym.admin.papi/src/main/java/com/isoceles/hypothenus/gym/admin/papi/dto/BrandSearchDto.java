package com.isoceles.hypothenus.gym.admin.papi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandSearchDto {
	
	private String brandId;
	
	private String name;
	
	private String email;
	
	private AddressDto address;
	
	private boolean isActive;
}
