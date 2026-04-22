package com.iso.hypo.brand.application.usecase;

import org.springframework.data.domain.Page;

import com.iso.hypo.brand.application.dto.CoachDto;
import com.iso.hypo.brand.domain.exception.CoachException;

public interface CoachQueryService {

    void assertExists(String brandUuid, String coachUuid) throws CoachException;

    CoachDto find(String brandUuid, String coachUuid) throws CoachException;

    Page<CoachDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws CoachException;
}