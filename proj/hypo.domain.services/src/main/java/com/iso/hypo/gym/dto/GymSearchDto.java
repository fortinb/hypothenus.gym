package com.iso.hypo.gym.dto;

import com.iso.hypo.common.domain.location.Address;

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
	
	private Address address;
	
	private boolean isActive;
}
