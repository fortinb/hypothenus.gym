package com.iso.hypo.model.repository;

import java.util.Optional;

import com.iso.hypo.model.aggregate.Coach;

public interface CoachRepositoryCustom {

	Optional<Coach> activate(String brandUuid, String gymUuid, String coachUuid);
	
	Optional<Coach> deactivate(String brandUuid, String gymUuid, String coachUuid);
}
