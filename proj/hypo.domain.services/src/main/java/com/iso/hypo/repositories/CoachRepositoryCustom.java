package com.iso.hypo.repositories;

import java.util.Optional;

import com.iso.hypo.domain.aggregate.Coach;

public interface CoachRepositoryCustom {

	Optional<Coach> activate(String brandUuid, String coachUuid);
	
	Optional<Coach> deactivate(String brandUuid, String coachUuid);
	
	void delete(String brandUuid, String coachUuid, String deletedBy);
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}
