package com.iso.hypo.admin.papi.dto.put;

import com.iso.hypo.admin.papi.dto.contact.PersonDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutCoachDto {
	
	@NotBlank
	private String brandUuid;
	
	@NotBlank
	private String gymUuid;
	
	@NotBlank
	private String uuid;
	
	private PersonDto person;
}
