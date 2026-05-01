package com.iso.hypo.membership.application.repository;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.membership.application.dto.search.MemberSearchDto;

public interface MemberQueryRepository {

	PageResult<MemberSearchDto> searchAutocomplete(String criteria, PageRequest pageRequest, boolean includeInactive);

}