package com.iso.hypo.brand.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.brand.application.dto.search.GymSearchDto;
import com.iso.hypo.brand.application.repository.GymQueryRepository;
import com.iso.hypo.brand.domain.model.Gym;
import com.iso.hypo.brand.domain.repository.GymRepository;
import com.iso.hypo.brand.infrastructure.persistence.entity.GymDocument;
import com.iso.hypo.brand.infrastructure.persistence.mapper.GymDocumentMapper;
import com.iso.hypo.brand.infrastructure.persistence.repository.GymMongoRepository;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;

@Repository
public class GymRepositoryAdapter extends BaseAdapter implements GymRepository, GymQueryRepository {

    private final GymMongoRepository mongoRepository;
    private final GymDocumentMapper gymMapper;

    public GymRepositoryAdapter(GymMongoRepository mongoRepository, GymDocumentMapper gymMapper) {
        this.mongoRepository = mongoRepository;
        this.gymMapper = gymMapper;
    }

    @Override
    public Optional<Gym> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String gymUuid) {
        return mongoRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, gymUuid)
                .map(gymMapper::toEntity);
    }

    @Override
    public Optional<Gym> findByBrandUuidAndCode(String brandUuid, String code) {
        return mongoRepository.findByBrandUuidAndCode(brandUuid, code)
                .map(gymMapper::toEntity);
    }

    @Override
    public PageResult<Gym> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest) {
        Page<GymDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, toSpringPageable(pageRequest, Sort.by("name").ascending()));
        return toPageResult(page, pageRequest).map(gymMapper::toEntity);
    }

    @Override
    public PageResult<Gym> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest) {
        Page<GymDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, toSpringPageable(pageRequest, Sort.by("name").ascending()));
        return toPageResult(page, pageRequest).map(gymMapper::toEntity);
    }

    @Override
    public Gym save(Gym gym) {
        GymDocument document = gymMapper.toDocument(gym);
        GymDocument saved = mongoRepository.save(document);
        return gymMapper.toEntity(saved);
    }

    @Override
    public void delete(Gym gym) {
        mongoRepository.delete(gymMapper.toDocument(gym));
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

    @Override
    public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
        return mongoRepository.deleteAllByBrandUuid(brandUuid, deletedBy);
    }

    @Override
    public long removeCoachReferences(String brandUuid, String coachUuid) {
        return mongoRepository.removeCoachReferences(brandUuid, coachUuid);
    }

    // --- Custom methods ---

    @Override
    public PageResult<GymSearchDto> searchAutocomplete(String criteria, PageRequest pageRequest, boolean includeInactive) {
        Page<GymSearchDto> page = mongoRepository.searchAutocomplete(criteria, toSpringPageable(pageRequest, Sort.by("name").ascending()), includeInactive);
        return toPageResult(page, pageRequest);
    }
}
