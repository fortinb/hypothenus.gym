package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.dto.search.GymSearchDto;

public interface GymRepositoryCustom {

	Page<GymSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Gym> activate(String brandUuid, String gymUuid);
	
	Optional<Gym> deactivate(String brandUuid, String gymUuid);
	
	void delete(String brandUuid, String gymUuid, String deletedBy);
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}

