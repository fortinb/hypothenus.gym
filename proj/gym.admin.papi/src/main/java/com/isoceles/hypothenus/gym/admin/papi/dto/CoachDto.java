package com.isoceles.hypothenus.gym.admin.papi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachDto extends BaseDto {
	
	private String id;
	
	private String gymId;
	
	private PersonDto person;
}
