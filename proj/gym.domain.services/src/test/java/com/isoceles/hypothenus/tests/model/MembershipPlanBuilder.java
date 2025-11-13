package com.isoceles.hypothenus.tests.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.domain.model.LanguageEnum;
import com.isoceles.hypothenus.gym.domain.model.LocalizedString;
import com.isoceles.hypothenus.gym.domain.model.MembershipPlanPaymentOptionEnum;
import com.isoceles.hypothenus.gym.domain.model.MembershipPlanPeriodEnum;
import com.isoceles.hypothenus.gym.domain.model.aggregate.MembershipPlan;

public class MembershipPlanBuilder {
	private static Faker faker = new Faker();
	
	public static MembershipPlan build(String brandId) {
		MembershipPlan entity = new MembershipPlan(brandId, faker.code().isbn10(), buildName(), buildDescription(),
				faker.number().numberBetween(2, 3), MembershipPlanPeriodEnum.month, MembershipPlanPaymentOptionEnum.monthly,
				150, 12, null, true, Instant.now(), null);
		return entity;
	}

	public static List<LocalizedString> buildName() {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(faker.esports().game(), LanguageEnum.fr));
		items.add(new LocalizedString(faker.esports().game(), LanguageEnum.en));

		return items;
	}
	
	public static List<LocalizedString> buildDescription() {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(faker.lorem().sentence(), LanguageEnum.fr));
		items.add(new LocalizedString(faker.lorem().sentence(), LanguageEnum.en));

		return items;
	}
}
