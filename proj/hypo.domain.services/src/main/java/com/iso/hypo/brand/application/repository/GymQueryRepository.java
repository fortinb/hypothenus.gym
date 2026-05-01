package com.iso.hypo.brand.application.repository;

import com.iso.hypo.brand.application.dto.search.GymSearchDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

public interface GymQueryRepository {
	
	PageResult<GymSearchDto> searchAutocomplete(String criteria, PageRequest pageRequest, boolean includeInactive);
}
