package com.iso.hypo.gym.admin.papi.dto.patch;

import com.iso.hypo.gym.admin.papi.dto.contact.PersonDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchCoachDto {
	
	@NotBlank
	private String uuid;
	
	@NotBlank
	private String brandId;
	
	@NotBlank
	private String gymId;
	
	private PersonDto person;
}
