package com.iso.hypo.brand.application.usecase;

import com.iso.hypo.brand.application.dto.BrandDto;
import com.iso.hypo.brand.application.exception.BrandException;

public interface BrandService {

    BrandDto create(BrandDto brandDto) throws BrandException;

    BrandDto update(BrandDto brandDto) throws BrandException;

    BrandDto patch(BrandDto brandDto) throws BrandException;

    void delete(String brandUuid) throws BrandException;

    BrandDto activate(String brandUuid) throws BrandException;

    BrandDto deactivate(String brandUuid) throws BrandException;
}


