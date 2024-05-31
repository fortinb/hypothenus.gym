package com.isoceles.hypothenus.gym.admin.papi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneNumber {
	private String regionalCode;
	private String number;
	private PhoneNumberTypeEnum type;
}
