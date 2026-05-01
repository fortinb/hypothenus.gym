package com.iso.hypo.brand.application.usecase;

import com.iso.hypo.brand.application.dto.GymDto;
import com.iso.hypo.brand.application.dto.search.GymSearchDto;
import com.iso.hypo.brand.application.exception.GymException;
import com.iso.hypo.common.application.dto.PageResultDto;

public interface GymQueryService {

    void assertExists(String brandUuid, String gymUuid) throws GymException;
    
    GymDto find(String brandUuid, String gymUuid) throws GymException;

    PageResultDto<GymSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws GymException;

    PageResultDto<GymDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws GymException;
}


