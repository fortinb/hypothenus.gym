package com.iso.hypo.tests.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.iso.hypo.gym.domain.model.LocalizedString;
import com.iso.hypo.gym.domain.model.aggregate.MembershipPlan;
import com.iso.hypo.gym.domain.model.enumeration.BillingFrequencyEnum;
import com.iso.hypo.gym.domain.model.enumeration.LanguageEnum;
import com.iso.hypo.gym.domain.model.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.gym.domain.model.pricing.Cost;
import com.iso.hypo.gym.domain.model.pricing.Currency;
import com.iso.hypo.gym.domain.model.pricing.OneTimeFee;

public class MembershipPlanBuilder {
	private static Faker faker = new Faker();
	
	public static MembershipPlan build(String brandId) {
		MembershipPlan entity = new MembershipPlan(brandId, faker.code().isbn10(), buildName(), buildDescription(),
				faker.number().numberBetween(2, 3), MembershipPlanPeriodEnum.monthly, BillingFrequencyEnum.monthly,
				BuildCost(), BuildOneTimeFees(), 12, null, null, true, false, false, true, Instant.now(), null);
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
	
	public static Cost BuildCost() {
		Cost item = new Cost(122,new Currency("Dollar","CA","$"));

		return item;
	}
	
	public static List<OneTimeFee> BuildOneTimeFees() {
		ArrayList<OneTimeFee> items = new ArrayList<OneTimeFee>();
		
		ArrayList<LocalizedString> description = new ArrayList<LocalizedString>();
		description.add(new LocalizedString(faker.lorem().sentence(), LanguageEnum.fr));
		description.add(new LocalizedString(faker.lorem().sentence(), LanguageEnum.en));
		
		items.add(new OneTimeFee(faker.code().isbn10(), description, BuildCost()));

		return items;
	}
}
