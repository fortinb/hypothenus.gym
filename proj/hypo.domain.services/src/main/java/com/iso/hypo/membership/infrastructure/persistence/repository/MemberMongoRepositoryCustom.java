package com.iso.hypo.membership.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.membership.application.dto.search.MemberSearchDto;

public interface MemberMongoRepositoryCustom {

    Page<MemberSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);

    void deleteAll();
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}