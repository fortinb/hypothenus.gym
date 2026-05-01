package com.iso.hypo.brand.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.brand.application.dto.search.GymSearchDto;

public interface GymMongoRepositoryCustom {

	Page<GymSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
    void deleteAll();
    
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
	
	long removeCoachReferences(String brandUuid, String coachUuid);
}