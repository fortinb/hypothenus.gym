package com.iso.hypo.domain;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.datafaker.Faker;
import com.iso.hypo.domain.LocalizedString;
import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.enumeration.LanguageEnum;

public class CourseBuilder {
	private static Faker faker = new Faker();
	
	public static Course build(String brandUuid, String gymUuid, List<Coach> coachs) {
		Course entity = new Course(brandUuid, gymUuid, faker.code().isbn10(), buildName(), buildDescription(),
				coachs, Date.from(Instant.now()), null,
				 true, Instant.now(), null);
		entity.setUuid(UUID.randomUUID().toString());
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
