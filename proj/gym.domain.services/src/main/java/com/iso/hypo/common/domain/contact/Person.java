package com.iso.hypo.common.domain.contact;

import java.util.Date;
import java.util.List;

import com.iso.hypo.common.domain.enumeration.LanguageEnum;
import com.iso.hypo.common.domain.location.Address;

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
	
	private List<Contact> contacts;
	
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
		this.contacts = emergencyContacts;
		this.photoUri = photoUri;
		this.communicationLanguage = communicationLanguage;
		this.note = note;
	}
}
