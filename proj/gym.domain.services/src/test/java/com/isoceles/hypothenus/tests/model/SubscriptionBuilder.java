package com.isoceles.hypothenus.tests.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.domain.model.LocalizedString;
import com.isoceles.hypothenus.gym.domain.model.SubscriptionPaymentOptionEnum;
import com.isoceles.hypothenus.gym.domain.model.SubscriptionPeriodEnum;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Subscription;

public class SubscriptionBuilder {
	private static Faker faker = new Faker();
	
	public static Subscription build(String gymId) {
		Subscription entity = new Subscription(gymId, faker.code().isbn10(), buildName(), buildDescription(),
				faker.number().numberBetween(2, 3), SubscriptionPeriodEnum.month, SubscriptionPaymentOptionEnum.monthly,
				150, 12, null, true, Instant.now(), null);
		return entity;
	}

	public static List<LocalizedString> buildName() {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(faker.esports().game(), "fr-CA"));
		items.add(new LocalizedString(faker.esports().game(), "en-US"));

		return items;
	}
	
	public static List<LocalizedString> buildDescription() {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(faker.lorem().sentence(), "fr-CA"));
		items.add(new LocalizedString(faker.lorem().sentence(), "en-US"));

		return items;
	}
}
