package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Coach;

public interface CoachRepositoryCustom {

	Optional<Coach> activate(String brandId, String gymId, String id);
	
	Optional<Coach> deactivate(String brandId, String gymId, String id);
}
