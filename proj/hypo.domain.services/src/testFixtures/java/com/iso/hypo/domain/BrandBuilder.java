package com.iso.hypo.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.contact.Contact;
import com.iso.hypo.domain.contact.PhoneNumber;
import com.iso.hypo.domain.enumeration.PhoneNumberTypeEnum;
import com.iso.hypo.domain.location.Address;

import net.datafaker.Faker;

public class BrandBuilder {
	private static Faker faker = new Faker();
	
	public static Brand build(String code, String companyName) {
		Brand entity = new Brand(code, companyName, buildAddress(),
				faker.internet().emailAddress(), faker.internet().image(), true, buildPhoneNumbers(), 
				buildContacts(), Instant.now(), null);
		entity.setUuid(UUID.randomUUID().toString());
		return entity;
	}

	public static Address buildAddress() {
		return new Address(faker.address().buildingNumber(), faker.address().streetName(), "35",
				faker.address().cityName(), faker.address().stateAbbr(), faker.address().zipCode());
	}

	public static List<PhoneNumber> buildPhoneNumbers() {
		ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(new PhoneNumber(faker.phoneNumber().cellPhone(), PhoneNumberTypeEnum.Mobile));
		phoneNumbers.add(new PhoneNumber(faker.phoneNumber().cellPhone(), PhoneNumberTypeEnum.Home));

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
