package com.iso.hypo.admin.papi.dto.model;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.contact.PersonDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachDto extends BaseDto {
	
	private String brandUuid;
	
	private String gymUuid;
	
	private String uuid;
	
	private PersonDto person;
}
