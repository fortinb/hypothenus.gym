package com.iso.hypo.repositories;

import java.util.Optional;

import com.iso.hypo.domain.aggregate.Coach;

public interface CoachRepositoryCustom {

	Optional<Coach> activate(String brandUuid, String gymUuid, String coachUuid);
	
	Optional<Coach> deactivate(String brandUuid, String gymUuid, String coachUuid);
}

