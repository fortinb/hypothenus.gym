package com.iso.hypo.brand.application.usecase;

import org.springframework.data.domain.Page;

import com.iso.hypo.brand.application.dto.BrandDto;
import com.iso.hypo.brand.application.dto.search.BrandSearchDto;
import com.iso.hypo.brand.domain.exception.BrandException;

public interface BrandQueryService {

    void assertExists(String brandUuid) throws BrandException;
    
    BrandDto find(String brandUuid) throws BrandException;
    
    BrandDto findByCode(String brandCode) throws BrandException;

    Page<BrandSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws BrandException;

    Page<BrandDto> list(int page, int pageSize, boolean includeInactive) throws BrandException;
}