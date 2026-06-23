package com.iso.hypo.brand.application.usecase;

import com.iso.hypo.brand.application.dto.BrandDto;
import com.iso.hypo.brand.application.dto.search.BrandSearchDto;
import com.iso.hypo.brand.application.exception.BrandException;
import com.iso.hypo.common.application.dto.PageResultDto;

public interface BrandQueryService {

    void assertExists(String brandUuid) throws BrandException;
    
    boolean assertDeleted(String brandUuid) throws BrandException;

    BrandDto find(String brandUuid) throws BrandException;

    BrandDto findByCode(String brandCode) throws BrandException;

    PageResultDto<BrandSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws BrandException;

    PageResultDto<BrandDto> list(int page, int pageSize, boolean includeInactive) throws BrandException;
}