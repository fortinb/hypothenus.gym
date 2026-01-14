package com.iso.hypo.gym.admin.papi.dto.put;

import com.iso.hypo.gym.admin.papi.dto.contact.PersonDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutCoachDto {
	
	@NotBlank
	private String uuid;
	
	@NotBlank
	private String brandId;
	
	@NotBlank
	private String gymId;
	
	private PersonDto person;
}
