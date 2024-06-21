package com.isoceles.hypothenus.tests.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumber;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumberTypeEnum;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Coach;

public class CoachBuilder {
	private static Faker faker = new Faker();
	
	public static Coach build(String gymId) {
		Coach entity = new Coach(gymId, faker.name().firstName(), faker.name().lastName(),
				faker.internet().emailAddress(), "fr-CA", buildPhoneNumbers(), true, Instant.now(), null);
		return entity;
	}

	public static List<PhoneNumber> buildPhoneNumbers() {
		ArrayList<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
		phoneNumbers.add(new PhoneNumber("514", faker.phoneNumber().cellPhone(), PhoneNumberTypeEnum.Mobile));
		phoneNumbers.add(new PhoneNumber("514", faker.phoneNumber().phoneNumber(), PhoneNumberTypeEnum.Home));

		return phoneNumbers;
	}
}
