package com.iso.hypo.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.domain.dto.CoachDto;
import com.iso.hypo.services.exception.CoachException;

public interface CoachQueryService {

    void assertExists(String brandUuid, String gymUuid, String coachUuid) throws CoachException;

    CoachDto find(String brandUuid, String gymUuid, String coachUuid) throws CoachException;

    Page<CoachDto> list(String brandUuid, String gymUuid, int page, int pageSize, boolean includeInactive) throws CoachException;
}


