package com.iso.hypo.membership.infrastructure.persistence.adapter;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;
import com.iso.hypo.membership.domain.model.MembershipPlan;
import com.iso.hypo.membership.domain.repository.MembershipPlanRepository;
import com.iso.hypo.membership.infrastructure.persistence.entity.MembershipPlanDocument;
import com.iso.hypo.membership.infrastructure.persistence.mapper.MembershipPlanDocumentMapper;
import com.iso.hypo.membership.infrastructure.persistence.repository.MembershipPlanMongoRepository;

@Repository
public class MembershipPlanRepositoryAdapter extends BaseAdapter implements MembershipPlanRepository {

    private final MembershipPlanMongoRepository mongoRepository;
    private final MembershipPlanDocumentMapper membershipPlanMapper;

    public MembershipPlanRepositoryAdapter(MembershipPlanMongoRepository mongoRepository, MembershipPlanDocumentMapper membershipPlanMapper) {
        this.mongoRepository = mongoRepository;
        this.membershipPlanMapper = membershipPlanMapper;
    }

    @Override
    public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
        return mongoRepository.deleteAllByBrandUuid(brandUuid, deletedBy);
    }

    @Override
    public Optional<MembershipPlan> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String membershipPlanUuid) {
        return mongoRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, membershipPlanUuid)
                .map(membershipPlanMapper::toEntity);
    }

    @Override
    public PageResult<MembershipPlan> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest) {
        Page<MembershipPlanDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, toSpringPageable(pageRequest, Sort.by("name").ascending()));
        return toPageResult(page, pageRequest).map(membershipPlanMapper::toEntity);
    }

    @Override
    public PageResult<MembershipPlan> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest) {
        Page<MembershipPlanDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, toSpringPageable(pageRequest, Sort.by("name").ascending()));
        return toPageResult(page, pageRequest).map(membershipPlanMapper::toEntity);
    }

    @Override
    public PageResult<MembershipPlan> findActiveOnDate(String brandUuid, Date currentDate, PageRequest pageRequest) {
        Page<MembershipPlanDocument> page = mongoRepository.findActiveOnDate(brandUuid, currentDate, toSpringPageable(pageRequest, Sort.by("name").ascending()));
        return toPageResult(page, pageRequest).map(membershipPlanMapper::toEntity);
    }

    @Override
    public long removeGymReferences(String brandUuid, String gymUuid) {
        return mongoRepository.removeGymReferences(brandUuid, gymUuid);
    }

    @Override
    public long removeCourseReferences(String brandUuid, String courseUuid) {
        return mongoRepository.removeCourseReferences(brandUuid, courseUuid);
    }

    @Override
    public MembershipPlan save(MembershipPlan membershipPlan) {
        MembershipPlanDocument document = membershipPlanMapper.toDocument(membershipPlan);
        MembershipPlanDocument saved = mongoRepository.save(document);
        return membershipPlanMapper.toEntity(saved);
    }

    @Override
    public void delete(MembershipPlan membershipPlan) {
        mongoRepository.delete(membershipPlanMapper.toDocument(membershipPlan));
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

}
