package com.isoceles.hypothenus.gym.admin.papi.dto.contact;

import java.util.Date;
import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.enumeration.LanguageEnum;
import com.isoceles.hypothenus.gym.admin.papi.dto.location.AddressDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonDto  {
	
	private String firstname;
	
	private String lastname;
	
	private Date dateOfBirth;

	private String email;
	
	private AddressDto address;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<ContactDto> contacts;
	
	private String photoUri;
	
	private LanguageEnum communicationLanguage;
	
	private String note;
}
