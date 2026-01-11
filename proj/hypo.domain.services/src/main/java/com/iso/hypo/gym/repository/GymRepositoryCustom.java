package com.iso.hypo.gym.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.gym.domain.aggregate.Gym;
import com.iso.hypo.gym.dto.GymSearchResult;

public interface GymRepositoryCustom {

	Page<GymSearchResult> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Gym> activate(String brandId, String gymId);
	
	Optional<Gym> deactivate(String brandId, String gymId);
}
