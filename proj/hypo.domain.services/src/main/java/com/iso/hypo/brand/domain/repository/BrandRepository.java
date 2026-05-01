package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

public interface BrandRepository {

    Optional<Brand> findByUuidAndDeletedIsFalse(String brandUuid);

    Optional<Brand> findByCode(String code);

    Optional<Brand> findByCodeAndDeletedIsFalse(String code);

    PageResult<Brand> findAllByDeletedIsFalse(PageRequest pageRequest);

    PageResult<Brand> findAllByDeletedIsFalseAndActiveIsTrue(PageRequest pageRequest);

    Brand save(Brand brand);

    void delete(Brand brand);

    void deleteAll();
}