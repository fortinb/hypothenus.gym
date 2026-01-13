package com.iso.hypo.brand.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.brand.dto.BrandSearchDto;
import com.iso.hypo.brand.domain.aggregate.Brand;

public interface BrandRepositoryCustom {

	Page<BrandSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	Optional<Brand> activate(String brandId);
	
	Optional<Brand> deactivate(String brandId);
}
