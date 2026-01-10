package com.iso.hypo.brand.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.brand.dto.BrandSearchResult;
import com.iso.hypo.brand.domain.aggregate.Brand;

public interface BrandRepositoryCustom {

	Page<BrandSearchResult> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Brand> activate(String brandId);
	
	Optional<Brand> deactivate(String brandId);
}
