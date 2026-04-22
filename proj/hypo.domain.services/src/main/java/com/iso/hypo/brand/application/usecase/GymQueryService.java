package com.iso.hypo.brand.application.usecase;

import org.springframework.data.domain.Page;

import com.iso.hypo.brand.application.dto.GymDto;
import com.iso.hypo.brand.application.dto.search.GymSearchDto;
import com.iso.hypo.brand.domain.exception.GymException;

public interface GymQueryService {

    void assertExists(String brandUuid, String gymUuid) throws GymException;
    
    GymDto find(String brandUuid, String gymUuid) throws GymException;

    Page<GymSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws GymException;

    Page<GymDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws GymException;
}


