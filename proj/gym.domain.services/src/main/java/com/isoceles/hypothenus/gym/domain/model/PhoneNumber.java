package com.isoceles.hypothenus.gym.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneNumber {
	private String regionalCode;
	
	private String number;
	
	private PhoneNumberTypeEnum type;
	
	public PhoneNumber() {
	}
	
	public PhoneNumber(String regionalCode, String number, PhoneNumberTypeEnum type) {
		super();
		this.regionalCode = regionalCode;
		this.number = number;
		this.type = type;
	}
}
