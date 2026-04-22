package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.brand.application.dto.search.GymSearchDto;
import com.iso.hypo.brand.domain.model.Gym;

public interface GymRepositoryCustom {

	Page<GymSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Gym> activate(String brandUuid, String gymUuid);
	
	Optional<Gym> deactivate(String brandUuid, String gymUuid);
	
	void delete(String brandUuid, String gymUuid, String deletedBy);
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
	
	long removeCoachReferences(String coachId);
}

