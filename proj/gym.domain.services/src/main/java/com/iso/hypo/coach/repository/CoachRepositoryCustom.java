package com.iso.hypo.coach.repository;

import java.util.Optional;

import com.iso.hypo.coach.domain.aggregate.Coach;

public interface CoachRepositoryCustom {

	Optional<Coach> activate(String brandId, String gymId, String id);
	
	Optional<Coach> deactivate(String brandId, String gymId, String id);
}
