package com.isoceles.hypothenus.gym.admin.papi.dto.contact;


import com.isoceles.hypothenus.gym.admin.papi.dto.enumeration.PhoneNumberTypeEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneNumberDto {
	
	@NotBlank
	private String number;
	
	@NotNull
	private PhoneNumberTypeEnum type;
}
