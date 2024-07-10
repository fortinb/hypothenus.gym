package com.isoceles.hypothenus.tests.model;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.domain.model.Address;
import com.isoceles.hypothenus.gym.domain.model.Contact;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumber;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumberTypeEnum;
import com.isoceles.hypothenus.gym.domain.model.SocialMediaAccount;
import com.isoceles.hypothenus.gym.domain.model.SocialMediaTypeEnum;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;

public class GymBuilder {
	private static Faker faker = new Faker();
	
	public static Gym build(String gymId) {
		Gym entity = new Gym(gymId, faker.company().name(), buildAddress(),
				faker.internet().emailAddress(), true, buildPhoneNumbers(), buildSocialMediaAccounts(),
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
	
	public static List<SocialMediaAccount> buildSocialMediaAccounts() {
		ArrayList<SocialMediaAccount> socialMediaAccounts = new ArrayList<SocialMediaAccount>();
		socialMediaAccounts.add(new SocialMediaAccount(SocialMediaTypeEnum.Facebook, faker.company().name(),
				URI.create(faker.company().url())));
		socialMediaAccounts.add(new SocialMediaAccount(SocialMediaTypeEnum.Instagram, faker.company().name(),
				URI.create(faker.company().url())));

		return socialMediaAccounts;
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
