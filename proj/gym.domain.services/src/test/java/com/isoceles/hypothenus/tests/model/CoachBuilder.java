package com.isoceles.hypothenus.tests.model;

import java.time.Instant;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Coach;

public class CoachBuilder {
	
	public static Coach build(String gymId) {
		Coach entity = new Coach(gymId, PersonBuilder.build(), true, Instant.now(), null);
		return entity;
	}
}
