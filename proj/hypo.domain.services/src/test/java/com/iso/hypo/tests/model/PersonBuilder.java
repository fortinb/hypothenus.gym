package com.iso.hypo.tests.model;

import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.iso.hypo.common.domain.contact.Contact;
import com.iso.hypo.common.domain.contact.Person;
import com.iso.hypo.common.domain.contact.PhoneNumber;
import com.iso.hypo.common.domain.enumeration.LanguageEnum;
import com.iso.hypo.common.domain.enumeration.PhoneNumberTypeEnum;
import com.iso.hypo.common.domain.location.Address;

public class PersonBuilder {
	private static Faker faker = new Faker();
	
	public static Person build() {
		Person entity = new Person(
				faker.name().firstName(), 
				faker.name().lastName(), 
				faker.date().birthday(),
				faker.internet().emailAddress(), 
				buildAddress(),
				buildPhoneNumbers(),
				buildContacts(),
				faker.internet().image(),
				LanguageEnum.fr,
				faker.lorem().sentence());
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
		contacts.add(new Contact(faker.name().firstName(), faker.name().lastName(),faker.cat().name(),
				faker.internet().emailAddress(), buildPhoneNumbers()));
		contacts.add(new Contact(faker.name().firstName(), faker.name().lastName(),faker.cat().name(),
				faker.internet().emailAddress(), buildPhoneNumbers()));

		return contacts;
	}
}
