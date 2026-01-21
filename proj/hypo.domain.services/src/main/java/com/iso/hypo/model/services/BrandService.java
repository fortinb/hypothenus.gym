package com.iso.hypo.model.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.model.dto.BrandDto;
import com.iso.hypo.model.dto.BrandSearchDto;
import com.iso.hypo.model.exception.BrandException;

public interface BrandService {

    BrandDto create(BrandDto brandDto) throws BrandException;

    BrandDto update(BrandDto brandDto) throws BrandException;

    BrandDto patch(BrandDto brandDto) throws BrandException;

    void delete(String brandUuid) throws BrandException;

    BrandDto findByBrandUuid(String brandUuid) throws BrandException;

    Page<BrandSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws BrandException;

    Page<BrandDto> list(int page, int pageSize, boolean includeInactive) throws BrandException;

    BrandDto activate(String brandUuid) throws BrandException;

    BrandDto deactivate(String brandUuid) throws BrandException;
}