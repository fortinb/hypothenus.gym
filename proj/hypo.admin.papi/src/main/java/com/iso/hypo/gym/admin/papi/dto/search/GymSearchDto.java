package com.iso.hypo.gym.admin.papi.dto.search;

import com.iso.hypo.gym.admin.papi.dto.location.AddressDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GymSearchDto {
	
    private String uuid;
	
	private String brandId;
	
	private String gymId;
	
	private String name;
	
	private String email;
	
	private AddressDto address;
	
	private boolean isActive;
}
