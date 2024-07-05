package com.isoceles.hypothenus.gym.admin.papi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GymSearchDto {
	
	private String gymId;
	
	private String name;
	
	private String email;
	
	private AddressDto address;
}
