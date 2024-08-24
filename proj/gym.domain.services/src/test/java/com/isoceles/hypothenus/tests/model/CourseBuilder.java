package com.isoceles.hypothenus.tests.model;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.github.javafaker.Faker;
import com.isoceles.hypothenus.gym.domain.model.LanguageEnum;
import com.isoceles.hypothenus.gym.domain.model.LocalizedString;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Coach;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Course;

public class CourseBuilder {
	private static Faker faker = new Faker();
	
	public static Course build(String gymId, List<Coach> coachs) {
		Course entity = new Course(gymId, faker.code().isbn10(), buildName(), buildDescription(),
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
