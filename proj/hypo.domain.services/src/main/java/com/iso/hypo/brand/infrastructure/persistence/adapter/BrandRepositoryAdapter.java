package com.iso.hypo.brand.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.repository.BrandRepository;
import com.iso.hypo.brand.infrastructure.persistence.entity.BrandDocument;
import com.iso.hypo.brand.infrastructure.persistence.mapper.BrandDocumentMapper;
import com.iso.hypo.brand.infrastructure.persistence.repository.BrandMongoRepository;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;

@Repository
public class BrandRepositoryAdapter extends BaseAdapter implements BrandRepository {

    private final BrandMongoRepository mongoRepository;
    private final BrandDocumentMapper brandMapper;

    public BrandRepositoryAdapter(BrandMongoRepository mongoRepository, BrandDocumentMapper brandMapper) {
        this.mongoRepository = mongoRepository;
        this.brandMapper = brandMapper;
    }

    @Override
    public Optional<Brand> findByUuidAndDeletedIsFalse(String brandUuid) {
        return mongoRepository.findByUuidAndDeletedIsFalse(brandUuid)
                .map(brandMapper::toEntity);
    }

    @Override
    public Optional<Brand> findByUuid(String brandUuid) {
        return mongoRepository.findByUuid(brandUuid)
                .map(brandMapper::toEntity);
    }
    
    @Override
    public Optional<Brand> findByCode(String code) {
        return mongoRepository.findByCode(code)
                .map(brandMapper::toEntity);
    }

    @Override
    public Optional<Brand> findByCodeAndDeletedIsFalse(String code) {
        return mongoRepository.findByCodeAndDeletedIsFalse(code)
                .map(brandMapper::toEntity);
    }

    @Override
    public PageResult<Brand> findAllByDeletedIsFalse(PageRequest pageRequest) {
        Page<BrandDocument> page = mongoRepository.findAllByDeletedIsFalse(toSpringPageable(pageRequest, Sort.by("name").ascending()));
        return toPageResult(page, pageRequest).map(brandMapper::toEntity);
    }

    @Override
    public PageResult<Brand> findAllByDeletedIsFalseAndActiveIsTrue(PageRequest pageRequest) {
        Page<BrandDocument> page = mongoRepository.findAllByDeletedIsFalseAndActiveIsTrue(toSpringPageable(pageRequest, Sort.by("name").ascending()));
        return toPageResult(page, pageRequest).map(brandMapper::toEntity);
    }

    @Override
    public Brand save(Brand brand) {
        BrandDocument document = brandMapper.toDocument(brand);
        BrandDocument saved = mongoRepository.save(document);
        return brandMapper.toEntity(saved);
    }

    @Override
    public void delete(Brand brand) {
        mongoRepository.delete(brandMapper.toDocument(brand));
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }
}