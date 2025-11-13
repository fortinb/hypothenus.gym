package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.isoceles.hypothenus.gym.domain.model.GymSearchResult;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;

public interface GymRepositoryCustom {

	Page<GymSearchResult> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Gym> activate(String brandId, String gymId);
	
	Optional<Gym> deactivate(String brandId, String gymId);
}
