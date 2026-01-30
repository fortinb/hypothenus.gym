package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.domain.dto.search.BrandSearchDto;
import com.iso.hypo.domain.aggregate.Brand;

public interface BrandRepositoryCustom {

	Page<BrandSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Brand> activate(String brandUuid);
	
	Optional<Brand> deactivate(String brandUuid);
	
	long delete(String brandUuid, String deletedBy);
}

