package com.iso.hypo.admin.papi.dto.post;

import com.iso.hypo.admin.papi.dto.contact.PersonDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCoachDto {
	
	@NotBlank
	private String brandUuid;
	
	@NotBlank
	private String gymUuid;
	
	private PersonDto person;
}
