package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.isoceles.hypothenus.gym.admin.papi.dto.PersonDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCoachDto {
	
	private String id;
	
	@NotBlank
	private String brandId;
	
	@NotBlank
	private String gymId;
	
	private PersonDto person;
}
