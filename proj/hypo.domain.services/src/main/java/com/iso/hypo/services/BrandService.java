package com.iso.hypo.services;

import com.iso.hypo.domain.dto.BrandDto;
import com.iso.hypo.services.exception.BrandException;

public interface BrandService {

    BrandDto create(BrandDto brandDto) throws BrandException;

    BrandDto update(BrandDto brandDto) throws BrandException;

    BrandDto patch(BrandDto brandDto) throws BrandException;

    void delete(String brandUuid) throws BrandException;

    BrandDto activate(String brandUuid) throws BrandException;

    BrandDto deactivate(String brandUuid) throws BrandException;
}


