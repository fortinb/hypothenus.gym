package com.isoceles.hypothenus.gym.admin.papi.dto.put;

import com.isoceles.hypothenus.gym.admin.papi.dto.PersonDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutCoachDto {
	
	@NotBlank
	private String id;
	
	@NotBlank
	private String brandId;
	
	@NotBlank
	private String gymId;
	
	private PersonDto person;
}
