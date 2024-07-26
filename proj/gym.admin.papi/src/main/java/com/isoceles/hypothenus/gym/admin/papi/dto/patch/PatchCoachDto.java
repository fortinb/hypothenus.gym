package com.isoceles.hypothenus.gym.admin.papi.dto.patch;

import com.isoceles.hypothenus.gym.admin.papi.dto.PersonDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchCoachDto {
	
	@NotBlank
	private String id;
	
	@NotBlank
	private String gymId;
	
	private PersonDto person;
}
