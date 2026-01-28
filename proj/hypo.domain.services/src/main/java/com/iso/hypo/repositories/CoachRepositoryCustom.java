package com.iso.hypo.repositories;

import java.util.Optional;

import com.iso.hypo.domain.aggregate.Coach;

public interface CoachRepositoryCustom {

	Optional<Coach> activate(String brandUuid, String gymUuid, String coachUuid);
	
	Optional<Coach> deactivate(String brandUuid, String gymUuid, String coachUuid);
	
	void delete(String brandUuid, String gymUuid, String coachUuid, String deletedBy);
	
	long deleteAllByGymUuid(String brandUuid, String gymUuid, String deletedBy);
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}

