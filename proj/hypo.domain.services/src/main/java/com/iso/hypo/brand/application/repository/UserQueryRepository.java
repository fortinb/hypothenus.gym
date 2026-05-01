package com.iso.hypo.brand.application.repository;

import com.iso.hypo.brand.application.dto.search.UserSearchDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

public interface UserQueryRepository {
	
	PageResult<UserSearchDto> searchAutocomplete(String criteria, PageRequest pageRequest, boolean includeInactive);
}
