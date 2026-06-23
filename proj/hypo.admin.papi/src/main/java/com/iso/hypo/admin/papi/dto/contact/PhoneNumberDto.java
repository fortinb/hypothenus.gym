package com.iso.hypo.admin.papi.dto.contact;

import com.iso.hypo.common.application.dto.enumeration.PhoneNumberTypeEnumDto;

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
	private PhoneNumberTypeEnumDto type;
}
