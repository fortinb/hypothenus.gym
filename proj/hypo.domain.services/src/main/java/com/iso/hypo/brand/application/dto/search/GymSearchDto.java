package com.iso.hypo.brand.application.dto.search;

import com.iso.hypo.common.application.dto.location.AddressDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GymSearchDto {
	
	private String brandUuid;
	
    private String uuid;
    
	private String code;
	
	private String name;
	
	private String email;
	
	private AddressDto address;
	
	private boolean active;
}