package com.iso.hypo.domain;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.domain.enumeration.BillingFrequencyEnum;
import com.iso.hypo.domain.enumeration.LanguageEnum;
import com.iso.hypo.domain.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.domain.pricing.Cost;
import com.iso.hypo.domain.pricing.Currency;

import net.datafaker.Faker;

public class MembershipPlanBuilder {
	private static Faker faker = new Faker();
	
	public static MembershipPlan build(String brandUuid, List<Gym> includedGyms, List<Course> includedCourses) {
		MembershipPlan entity = new MembershipPlan(brandUuid, buildName(), buildTitle(), buildDescription(),
				faker.number().numberBetween(2, 3), MembershipPlanPeriodEnum.monthly, BillingFrequencyEnum.monthly,
				BuildCost(), 12, includedGyms, includedCourses, Date.from(Instant.now()), null, true, false, false, true, Instant.now(), null);
		entity.setUuid(UUID.randomUUID().toString());
		return entity;
	}

	public static List<LocalizedString> buildName() {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(faker.esports().game(), LanguageEnum.fr));
		items.add(new LocalizedString(faker.esports().game(), LanguageEnum.en));
		return items;
	}

	public static List<LocalizedString> buildTitle() {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(faker.marketing().buzzwords(), LanguageEnum.fr));
		items.add(new LocalizedString(faker.marketing().buzzwords(), LanguageEnum.en));
		return items;
	}

	public static List<LocalizedString> buildDescription() {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(faker.lorem().sentence(), LanguageEnum.fr));
		items.add(new LocalizedString(faker.lorem().sentence(), LanguageEnum.en));

		return items;
	}
	
	public static Cost BuildCost() {
		Cost item = new Cost(122,new Currency("Canadian dollar","CAD","$"));

		return item;
	}
}
