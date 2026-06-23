package com.iso.hypo.membership.infrastructure.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.common.infrastructure.persistence.adapter.BaseAdapter;
import com.iso.hypo.membership.domain.model.Member;
import com.iso.hypo.membership.domain.repository.MemberRepository;
import com.iso.hypo.membership.infrastructure.persistence.entity.MemberDocument;
import com.iso.hypo.membership.infrastructure.persistence.mapper.MemberDocumentMapper;
import com.iso.hypo.membership.infrastructure.persistence.repository.MemberMongoRepository;

@Repository
public class MemberRepositoryAdapter extends BaseAdapter implements MemberRepository {

    private final MemberMongoRepository mongoRepository;
    private final MemberDocumentMapper memberMapper;

    public MemberRepositoryAdapter(MemberMongoRepository mongoRepository, MemberDocumentMapper memberMapper) {
        this.mongoRepository = mongoRepository;
        this.memberMapper = memberMapper;
    }

    @Override
    public long deleteAllByBrandUuid(String brandUuid, String deletedBy) {
        return mongoRepository.deleteAllByBrandUuid(brandUuid, deletedBy);
    }

    @Override
    public Optional<Member> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid) {
        return mongoRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid)
                .map(memberMapper::toEntity);
    }
    
    @Override
    public Optional<Member> findByBrandUuidAndUuid(String brandUuid, String memberUuid) {
        return mongoRepository.findByBrandUuidAndUuid(brandUuid, memberUuid)
                .map(memberMapper::toEntity);
    }

    @Override
    public PageResult<Member> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest) {
        Page<MemberDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, toSpringPageable(pageRequest, Sort.by("person.lastName").ascending()));
        return toPageResult(page, pageRequest).map(memberMapper::toEntity);
    }

    @Override
    public PageResult<Member> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest) {
        Page<MemberDocument> page = mongoRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, toSpringPageable(pageRequest, Sort.by("person.lastName").ascending()));
        return toPageResult(page, pageRequest).map(memberMapper::toEntity);
    }

    @Override
    public Optional<Member> findByBrandUuidAndPersonEmailAndDeletedIsFalse(String brandUuid, String email) {
        return mongoRepository.findByBrandUuidAndPersonEmailAndDeletedIsFalse(brandUuid, email)
                .map(memberMapper::toEntity);
    }

    @Override
    public Optional<Member> findByBrandUuidAndUserUuidAndDeletedIsFalse(String brandUuid, String userUuid) {
        return mongoRepository.findByBrandUuidAndUserUuidAndDeletedIsFalse(brandUuid, userUuid)
                .map(memberMapper::toEntity);
    }

    @Override
    public Member save(Member member) {
        MemberDocument document = memberMapper.toDocument(member);
        MemberDocument saved = mongoRepository.save(document);
        return memberMapper.toEntity(saved);
    }

    @Override
    public void delete(Member member) {
        mongoRepository.delete(memberMapper.toDocument(member));
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

}
