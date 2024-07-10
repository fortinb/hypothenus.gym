package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactDto  {
	
	private String firstname;
	
	private String lastname;
	
	private String description;
	
	private String email;
	
	private List<PhoneNumberDto> phoneNumbers;
}
