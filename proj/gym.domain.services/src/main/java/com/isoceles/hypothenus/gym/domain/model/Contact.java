package com.isoceles.hypothenus.gym.domain.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Contact {
	
	private String firstname;
	
	private String lastname;
	
	private String description;
	
	private String email;
	
	private List<PhoneNumber> phoneNumbers;
	
	public Contact() {
	}
	
	public Contact(String firstname, String lastname, String description, String email, List<PhoneNumber> phoneNumbers) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.description = description;
		this.email = email;
		this.phoneNumbers = phoneNumbers;
	}
}
