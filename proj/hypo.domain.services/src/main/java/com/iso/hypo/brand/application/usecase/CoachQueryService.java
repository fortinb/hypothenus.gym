package com.iso.hypo.brand.application.usecase;

import com.iso.hypo.brand.application.dto.CoachDto;
import com.iso.hypo.brand.application.exception.CoachException;
import com.iso.hypo.common.application.dto.PageResultDto;

public interface CoachQueryService {

    void assertExists(String brandUuid, String coachUuid) throws CoachException;

    CoachDto find(String brandUuid, String coachUuid) throws CoachException;

    PageResultDto<CoachDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws CoachException;
}