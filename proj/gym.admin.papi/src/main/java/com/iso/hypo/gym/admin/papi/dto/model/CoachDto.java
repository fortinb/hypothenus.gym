package com.iso.hypo.gym.admin.papi.dto.model;

import com.iso.hypo.gym.admin.papi.dto.BaseDto;
import com.iso.hypo.gym.admin.papi.dto.contact.PersonDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachDto extends BaseDto {
	
	private String id;
	
	private String brandId;
	
	private String gymId;
	
	private PersonDto person;
}
