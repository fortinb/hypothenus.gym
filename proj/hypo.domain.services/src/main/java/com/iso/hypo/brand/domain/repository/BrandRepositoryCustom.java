package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.brand.application.dto.search.BrandSearchDto;
import com.iso.hypo.brand.domain.model.Brand;

public interface BrandRepositoryCustom {

	Page<BrandSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Brand> activate(String brandUuid);
	
	Optional<Brand> deactivate(String brandUuid);
	
	long delete(String brandUuid, String deletedBy);
}

