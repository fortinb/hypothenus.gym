package com.isoceles.hypothenus.tests.model;

import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.domain.model.Address;
import com.isoceles.hypothenus.gym.domain.model.Contact;
import com.isoceles.hypothenus.gym.domain.model.LanguageEnum;
import com.isoceles.hypothenus.gym.domain.model.Person;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumber;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumberTypeEnum;

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
		contacts.add(new Contact(faker.name().firstName(), faker.name().lastName(),faker.gameOfThrones().dragon(),
				faker.internet().emailAddress(), buildPhoneNumbers()));
		contacts.add(new Contact(faker.name().firstName(), faker.name().lastName(),faker.gameOfThrones().dragon(),
				faker.internet().emailAddress(), buildPhoneNumbers()));

		return contacts;
	}
}
