package com.iso.hypo.membership.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;
import com.iso.hypo.membership.domain.model.Membership;
import com.iso.hypo.membership.domain.repository.MembershipRepository;
import com.iso.hypo.membership.infrastructure.persistence.entity.MembershipDocument;
import com.iso.hypo.membership.infrastructure.persistence.mapper.MembershipDocumentMapper;
import com.iso.hypo.membership.infrastructure.persistence.repository.MembershipMongoRepository;

@Repository
public class MembershipRepositoryAdapter extends BaseAdapter implements MembershipRepository {

    private final MembershipMongoRepository mongoRepository;
    private final MembershipDocumentMapper membershipMapper;

    public MembershipRepositoryAdapter(MembershipMongoRepository mongoRepository, MembershipDocumentMapper membershipMapper) {
        this.mongoRepository = mongoRepository;
        this.membershipMapper = membershipMapper;
    }

    @Override
    public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
        return mongoRepository.deleteAllByBrandUuid(brandUuid, deletedBy);
    }

    @Override
    public Optional<Membership> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String membershipUuid) {
        return mongoRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, membershipUuid)
                .map(membershipMapper::toEntity);
    }

    @Override
    public PageResult<Membership> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest) {
        Page<MembershipDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, toSpringPageable(pageRequest, Sort.by("name").ascending()));
        return toPageResult(page, pageRequest).map(membershipMapper::toEntity);
    }

    @Override
    public PageResult<Membership> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest) {
        Page<MembershipDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, toSpringPageable(pageRequest, Sort.by("name").ascending()));
        return toPageResult(page, pageRequest).map(membershipMapper::toEntity);
    }

    @Override
    public Membership save(Membership membership) {
        MembershipDocument document = membershipMapper.toDocument(membership);
        MembershipDocument saved = mongoRepository.save(document);
        return membershipMapper.toEntity(saved);
    }

    @Override
    public void delete(Membership membership) {
        mongoRepository.delete(membershipMapper.toDocument(membership));
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

	@Override
	public PageResult<Membership> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid, String memberUuid,	PageRequest pageRequest) {
        Page<MembershipDocument> page = mongoRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(brandUuid, memberUuid, toSpringPageable(pageRequest, Sort.by("activatedOn").ascending()));
        return toPageResult(page, pageRequest).map(membershipMapper::toEntity);
	}

	@Override
	public Optional<Membership> findByMembershipPlanUuid(String brandUuid, String memberUuid, String membershipPlanUuid) {
		   return mongoRepository.findByMembershipPlanUuid(brandUuid, memberUuid, membershipPlanUuid)
	                .map(membershipMapper::toEntity);
	}

}
