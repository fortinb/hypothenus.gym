package com.isoceles.hypothenus.gym.domain.model;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Person {
	
	private String firstname;
	
	private String lastname;
	
	private Date dateOfBirth;
	
	private String email;
	
	private Address address;
	
	private List<PhoneNumber> phoneNumbers;
	
	private List<Contact> emergencyContacts;
	
	private String photoUri;
	
	private LanguageEnum communicationLanguage;
	
	private String note;
	
	public Person() {
	}
	
	public Person(String firstname, String lastname, Date dateOfBirth, String email, Address address, List<PhoneNumber> phoneNumbers, List<Contact> emergencyContacts, String photoUri,
			LanguageEnum communicationLanguage, String note) {
		super();
		this.firstname = firstname;
		this.lastname = lastname;
		this.dateOfBirth = dateOfBirth;
		this.email = email;
		this.address = address;
		this.phoneNumbers = phoneNumbers;
		this.emergencyContacts = emergencyContacts;
		this.photoUri = photoUri;
		this.communicationLanguage = communicationLanguage;
		this.note = note;
	}
}
