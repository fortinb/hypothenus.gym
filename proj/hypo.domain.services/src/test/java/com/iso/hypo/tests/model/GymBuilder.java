package com.iso.hypo.tests.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.iso.hypo.gym.domain.aggregate.Gym;
import com.iso.hypo.common.domain.contact.Contact;
import com.iso.hypo.common.domain.contact.PhoneNumber;
import com.iso.hypo.common.domain.enumeration.PhoneNumberTypeEnum;
import com.iso.hypo.common.domain.location.Address;

public class GymBuilder {
	private static Faker faker = new Faker();
	
	public static Gym build(String brandId, String gymId, String gymName) {
		Gym entity = new Gym(brandId, gymId, gymName, buildAddress(),
				faker.internet().emailAddress(), faker.internet().image(), true, buildPhoneNumbers(), 
				buildContacts(), Instant.now(), null);
		return entity;
	}

	public static Address buildAddress() {
		return new Address(faker.address().buildingNumber(), faker.address().streetName(), "35",
				faker.address().cityName(), faker.address().stateAbbr(), faker.address().zipCode());
	}

	public static List<PhoneNumber> buildPhoneNumbers() {
		ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(new PhoneNumber(faker.phoneNumber().cellPhone(), PhoneNumberTypeEnum.Mobile));
		phoneNumbers.add(new PhoneNumber(faker.phoneNumber().phoneNumber(), PhoneNumberTypeEnum.Home));

		return phoneNumbers;
	}
	

	public static List<Contact> buildContacts() {
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		contacts.add(new Contact(faker.name().firstName(), faker.name().lastName(),faker.company().profession(),
				faker.internet().emailAddress(), buildPhoneNumbers()));
		contacts.add(new Contact(faker.name().firstName(), faker.name().lastName(),faker.company().profession(),
				faker.internet().emailAddress(), buildPhoneNumbers()));
		contacts.add(new Contact(faker.name().firstName(), faker.name().lastName(),faker.company().profession(),
				faker.internet().emailAddress(), buildPhoneNumbers()));

		return contacts;
	}
}
