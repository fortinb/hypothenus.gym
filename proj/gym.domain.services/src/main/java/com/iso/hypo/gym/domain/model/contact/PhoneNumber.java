package com.iso.hypo.gym.domain.model.contact;

import com.iso.hypo.gym.domain.model.enumeration.PhoneNumberTypeEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneNumber {
	
	private String number;
	
	private PhoneNumberTypeEnum type;
	
	public PhoneNumber() {
	}
	
	public PhoneNumber(String number, PhoneNumberTypeEnum type) {
		super();
		this.number = number;
		this.type = type;
	}
}
