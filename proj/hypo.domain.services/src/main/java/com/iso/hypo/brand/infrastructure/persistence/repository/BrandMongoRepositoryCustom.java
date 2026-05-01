package com.iso.hypo.brand.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.brand.application.dto.search.BrandSearchDto;

public interface BrandMongoRepositoryCustom {

    Page<BrandSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);

    void deleteAll();
}