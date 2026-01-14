package com.iso.hypo.gym.admin.papi.dto.post;

import com.iso.hypo.gym.admin.papi.dto.contact.PersonDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCoachDto {
	
	@NotBlank
	private String brandId;
	
	@NotBlank
	private String gymId;
	
	private PersonDto person;
}
