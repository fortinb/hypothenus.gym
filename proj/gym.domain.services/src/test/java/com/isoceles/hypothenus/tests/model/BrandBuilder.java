package com.isoceles.hypothenus.tests.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Brand;
import com.isoceles.hypothenus.gym.domain.model.contact.Contact;
import com.isoceles.hypothenus.gym.domain.model.contact.PhoneNumber;
import com.isoceles.hypothenus.gym.domain.model.enumeration.PhoneNumberTypeEnum;
import com.isoceles.hypothenus.gym.domain.model.location.Address;

public class BrandBuilder {
	private static Faker faker = new Faker();
	
	public static Brand build(String brandId, String companyName) {
		Brand entity = new Brand(brandId, companyName, buildAddress(),
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
		contacts.add(new Contact(faker.name().firstName(), faker.name().lastName(),faker.gameOfThrones().dragon(),
				faker.internet().emailAddress(), buildPhoneNumbers()));
		contacts.add(new Contact(faker.name().firstName(), faker.name().lastName(),faker.gameOfThrones().dragon(),
				faker.internet().emailAddress(), buildPhoneNumbers()));
		contacts.add(new Contact(faker.name().firstName(), faker.name().lastName(),faker.gameOfThrones().dragon(),
				faker.internet().emailAddress(), buildPhoneNumbers()));

		return contacts;
	}
}
