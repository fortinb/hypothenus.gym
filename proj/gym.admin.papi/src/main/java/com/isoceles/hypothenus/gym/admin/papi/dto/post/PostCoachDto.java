package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import com.isoceles.hypothenus.gym.admin.papi.dto.PersonDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCoachDto {
	
	@NotBlank
	private String gymId;
	
	private PersonDto person;
}
