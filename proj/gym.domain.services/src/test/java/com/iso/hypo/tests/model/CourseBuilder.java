package com.iso.hypo.tests.model;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.iso.hypo.common.domain.LocalizedString;
import com.iso.hypo.coach.domain.aggregate.Coach;
import com.iso.hypo.course.domain.aggregate.Course;
import com.iso.hypo.common.domain.enumeration.LanguageEnum;

public class CourseBuilder {
	private static Faker faker = new Faker();
	
	public static Course build(String brandId, String gymId, List<Coach> coachs) {
		Course entity = new Course(brandId, gymId, faker.code().isbn10(), buildName(), buildDescription(),
				coachs, Date.from(Instant.now()), Date.from(Instant.now()),
				 true, Instant.now(), null);
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
