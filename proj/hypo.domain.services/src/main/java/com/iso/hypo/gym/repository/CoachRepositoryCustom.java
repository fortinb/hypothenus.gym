package com.iso.hypo.gym.repository;

import java.util.Optional;

import com.iso.hypo.gym.domain.aggregate.Coach;

public interface CoachRepositoryCustom {

	Optional<Coach> activate(String brandId, String gymId, String id);
	
	Optional<Coach> deactivate(String brandId, String gymId, String id);
}
