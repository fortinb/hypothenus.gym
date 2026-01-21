package com.iso.hypo.model.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.model.dto.BrandSearchDto;
import com.iso.hypo.model.aggregate.Brand;

public interface BrandRepositoryCustom {

	Page<BrandSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Brand> activate(String brandUuid);
	
	Optional<Brand> deactivate(String brandUuid);
}
