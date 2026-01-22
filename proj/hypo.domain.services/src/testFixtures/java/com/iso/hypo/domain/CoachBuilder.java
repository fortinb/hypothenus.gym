package com.iso.hypo.domain;

import java.time.Instant;
import java.util.UUID;

import com.iso.hypo.domain.aggregate.Coach;

public class CoachBuilder {
	
	public static Coach build(String brandUuid, String gymUuid) {
		Coach entity = new Coach(brandUuid, gymUuid, PersonBuilder.build(), true, Instant.now(), null);
		entity.setUuid(UUID.randomUUID().toString());
		return entity;
	}
}
