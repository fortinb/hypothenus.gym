package com.iso.hypo.domain;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.iso.hypo.common.domain.model.LocalizedString;
import com.iso.hypo.common.domain.model.enumeration.LanguageEnum;
import com.iso.hypo.common.domain.model.finance.Cost;
import com.iso.hypo.common.domain.model.finance.Currency;
import com.iso.hypo.membership.domain.model.MembershipPlan;
import com.iso.hypo.membership.domain.model.enumeration.BillingFrequencyEnum;
import com.iso.hypo.membership.domain.model.enumeration.MembershipPlanPeriodEnum;

import net.datafaker.Faker;

public class MembershipPlanBuilder {
	private static Faker faker = new Faker();
	
	public static MembershipPlan build(String brandUuid, List<String> includedGymUuids, List<String> includedCourseUuids) {
		MembershipPlan entity = new MembershipPlan(brandUuid, buildName(), buildTitle(), buildDescription(), buildTermsOfUse(),
				faker.number().numberBetween(2, 3), MembershipPlanPeriodEnum.monthly, BillingFrequencyEnum.monthly,
				BuildPrice(), 12, includedGymUuids, includedCourseUuids, Date.from(Instant.now().truncatedTo(ChronoUnit.DAYS)), null, true, false, false, true, Instant.now(), null);
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

	public static List<LocalizedString> buildTermsOfUse() {
		ArrayList<LocalizedString> items = new ArrayList<LocalizedString>();
		items.add(new LocalizedString(faker.lorem().paragraph(), LanguageEnum.fr));
		items.add(new LocalizedString(faker.lorem().paragraph(), LanguageEnum.en));

		return items;
	}
	
	public static Cost BuildPrice() {
		Cost item = new Cost(15000,new Currency("Canadian dollar","CAD","$"));

		return item;
	}
}
