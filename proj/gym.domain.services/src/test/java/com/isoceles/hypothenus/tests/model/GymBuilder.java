package com.isoceles.hypothenus.tests.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.domain.model.Address;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumber;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumberTypeEnum;
import com.isoceles.hypothenus.gym.domain.model.SocialMediaAccount;
import com.isoceles.hypothenus.gym.domain.model.SocialMediaTypeEnum;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;

public class GymBuilder {
	private static Faker faker = new Faker();
	
	public static Gym build() {
		Gym entity = new Gym(faker.code().isbn10(), faker.company().name(), buildAddress(),
				faker.internet().emailAddress(), "fr-CA", buildPhoneNumbers(), buildSocialMediaAccounts());
		return entity;

	}

	public static Address buildAddress() {
		return new Address(faker.address().buildingNumber(), faker.address().streetName(), "35",
				faker.address().cityName(), faker.address().stateAbbr(), faker.address().zipCode());
	}

	public static List<PhoneNumber> buildPhoneNumbers() {
		ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(new PhoneNumber("514", faker.phoneNumber().cellPhone(), PhoneNumberTypeEnum.Mobile));
		phoneNumbers.add(new PhoneNumber("514", faker.phoneNumber().phoneNumber(), PhoneNumberTypeEnum.Home));

		return phoneNumbers;
	}
	
	public static List<SocialMediaAccount> buildSocialMediaAccounts() {
		ArrayList<SocialMediaAccount> socialMediaAccount = new ArrayList<SocialMediaAccount>();
		socialMediaAccount.add(new SocialMediaAccount(SocialMediaTypeEnum.Facebook, faker.company().name(),
				URI.create(faker.company().url())));
		socialMediaAccount.add(new SocialMediaAccount(SocialMediaTypeEnum.Instagram, faker.company().name(),
				URI.create(faker.company().url())));

		return socialMediaAccount;
	}
}
