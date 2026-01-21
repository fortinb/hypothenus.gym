package com.iso.hypo.model.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.model.aggregate.Gym;
import com.iso.hypo.model.dto.GymSearchDto;

public interface GymRepositoryCustom {

	Page<GymSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Gym> activate(String brandUuid, String gymUuid);
	
	Optional<Gym> deactivate(String brandUuid, String gymUuid);
}
