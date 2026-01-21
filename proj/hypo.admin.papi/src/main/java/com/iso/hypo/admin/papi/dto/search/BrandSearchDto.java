package com.iso.hypo.admin.papi.dto.search;

import com.iso.hypo.admin.papi.dto.location.AddressDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandSearchDto {
	
    private String uuid;
	
	private String code;
	
	private String name;
	
	private String email;
	
	private AddressDto address;
	
	private boolean isActive;
}
