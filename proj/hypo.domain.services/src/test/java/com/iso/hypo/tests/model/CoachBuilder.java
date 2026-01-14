package com.iso.hypo.tests.model;

import java.time.Instant;
import java.util.UUID;

import com.iso.hypo.gym.domain.aggregate.Coach;

public class CoachBuilder {
	
	public static Coach build(String brandId, String gymId) {
		Coach entity = new Coach(brandId, gymId, PersonBuilder.build(), true, Instant.now(), null);
		entity.setUuid(UUID.randomUUID().toString());
		return entity;
	}
}
