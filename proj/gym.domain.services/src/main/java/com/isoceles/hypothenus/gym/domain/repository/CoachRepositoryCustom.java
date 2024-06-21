package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Coach;

public interface CoachRepositoryCustom {

	Optional<Coach> activate(String gymId, String id);
	
	Optional<Coach> deactivate(String gymId, String id);
}
