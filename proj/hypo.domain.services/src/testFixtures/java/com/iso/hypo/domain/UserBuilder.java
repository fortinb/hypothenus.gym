package com.iso.hypo.domain;

import java.time.Instant;
import java.util.UUID;

import com.iso.hypo.domain.aggregate.User;

import net.datafaker.Faker;

public class UserBuilder {
	private static Faker faker = new Faker();

	public static User build() {
		User entity = new User(faker.name().firstName(), faker.name().lastName(), faker.internet().emailAddress(), true,
				Instant.now(), null);
		entity.setUuid(UUID.randomUUID().toString());
		return entity;
	}
}