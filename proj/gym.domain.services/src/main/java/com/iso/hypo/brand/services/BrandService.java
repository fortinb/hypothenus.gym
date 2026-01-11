package com.iso.hypo.brand.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.brand.dto.BrandDto;
import com.iso.hypo.brand.dto.BrandSearchResult;
import com.iso.hypo.brand.exception.BrandException;

public interface BrandService {

    BrandDto create(BrandDto brandDto) throws BrandException;

    BrandDto update(BrandDto brandDto) throws BrandException;

    BrandDto patch(BrandDto brandDto) throws BrandException;

    void delete(String brandId) throws BrandException;

    BrandDto findByBrandId(String id) throws BrandException;

    Page<BrandSearchResult> search(int page, int pageSize, String criteria, boolean includeInactive) throws BrandException;

    Page<BrandDto> list(int page, int pageSize, boolean includeInactive) throws BrandException;

    BrandDto activate(String brandId) throws BrandException;

    BrandDto deactivate(String brandId) throws BrandException;
}