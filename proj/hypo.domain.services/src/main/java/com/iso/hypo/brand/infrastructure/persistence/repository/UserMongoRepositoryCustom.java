package com.iso.hypo.brand.infrastructure.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.brand.application.dto.search.UserSearchDto;

public interface UserMongoRepositoryCustom {

	Page<UserSearchDto> searchAutocomplete(String criteria, Pageable pageable, boolean includeInactive);
	
	void deleteAll();
}