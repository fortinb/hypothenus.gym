package com.iso.hypo.brand.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.brand.domain.model.Coach;
import com.iso.hypo.brand.domain.repository.CoachRepository;
import com.iso.hypo.brand.infrastructure.persistence.entity.CoachDocument;
import com.iso.hypo.brand.infrastructure.persistence.mapper.CoachDocumentMapper;
import com.iso.hypo.brand.infrastructure.persistence.repository.CoachMongoRepository;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;

@Repository
public class CoachRepositoryAdapter extends BaseAdapter implements CoachRepository {

    private final CoachMongoRepository mongoRepository;
    private final CoachDocumentMapper coachMapper;

    public CoachRepositoryAdapter(CoachMongoRepository mongoRepository, CoachDocumentMapper coachMapper) {
        this.mongoRepository = mongoRepository;
        this.coachMapper = coachMapper;
    }

    @Override
    public Optional<Coach> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String coachUuid) {
        return mongoRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, coachUuid)
                .map(coachMapper::toEntity);
    }

    @Override
    public PageResult<Coach> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest) {
        Page<CoachDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, toSpringPageable(pageRequest, Sort.by("person.lastname").ascending()));
        return toPageResult(page, pageRequest).map(coachMapper::toEntity);
    }

    @Override
    public PageResult<Coach> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest) {
        Page<CoachDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, toSpringPageable(pageRequest,Sort.by("person.lastname").ascending()));
        return toPageResult(page, pageRequest).map(coachMapper::toEntity);
    }

    @Override
    public Coach save(Coach coach) {
        CoachDocument document = coachMapper.toDocument(coach);
        CoachDocument saved = mongoRepository.save(document);
        return coachMapper.toEntity(saved);
    }

    @Override
    public void delete(Coach coach) {
        mongoRepository.delete(coachMapper.toDocument(coach));
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

    @Override
    public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
        return mongoRepository.deleteAllByBrandUuid(brandUuid, deletedBy);
    }
}
