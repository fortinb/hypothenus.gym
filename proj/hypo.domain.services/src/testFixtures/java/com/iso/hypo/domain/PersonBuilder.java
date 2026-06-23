package com.iso.hypo.domain;

import java.util.ArrayList;
import java.util.List;

import net.datafaker.Faker;

import com.iso.hypo.common.domain.model.contact.Contact;
import com.iso.hypo.common.domain.model.contact.Person;
import com.iso.hypo.common.domain.model.contact.PhoneNumber;
import com.iso.hypo.common.domain.model.enumeration.LanguageEnum;
import com.iso.hypo.common.domain.model.enumeration.PhoneNumberTypeEnum;
import com.iso.hypo.common.domain.model.location.Address;

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
				faker.internet().image(200,200),
				LanguageEnum.fr,
				faker.lorem().sentence());
		return entity;
	}

	public static Address buildAddress() {
		return new Address(faker.address().buildingNumber(), faker.address().streetName(), "35",
				faker.address().cityName(), "CA", "QC", faker.address().zipCode());
	}
	
	public static List<PhoneNumber> buildPhoneNumbers() {
		ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(new PhoneNumber(faker.phoneNumber().cellPhone(), PhoneNumberTypeEnum.home));
		phoneNumbers.add(new PhoneNumber(faker.phoneNumber().cellPhone(), PhoneNumberTypeEnum.mobile));

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
