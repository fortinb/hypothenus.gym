package com.iso.hypo.domain;

import java.time.Instant;
import java.util.UUID;

import com.iso.hypo.domain.aggregate.Coach;

public class CoachBuilder {
	
	public static Coach build(String brandUuid) {
		Coach entity = new Coach(brandUuid, PersonBuilder.build(), true, Instant.now(), null);
		entity.setUuid(UUID.randomUUID().toString());
		return entity;
	}
}