package com.iso.hypo.domain.dto.search;

import com.iso.hypo.domain.location.Address;

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
	
	private Address address;
	
	private boolean isActive;
}