package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.isoceles.hypothenus.gym.domain.model.BrandSearchResult;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Brand;

public interface BrandRepositoryCustom {

	Page<BrandSearchResult> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Brand> activate(String brandId);
	
	Optional<Brand> deactivate(String brandId);
}
