package com.iso.hypo.services.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.dto.MemberDto;
import com.iso.hypo.domain.dto.search.MemberSearchDto;
import com.iso.hypo.repositories.MemberRepository;
import com.iso.hypo.services.MemberQueryService;
import com.iso.hypo.services.exception.MemberException;
import com.iso.hypo.services.mappers.MemberMapper;

@Service
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;

    private final MemberMapper memberMapper;

    private static final Logger logger = LoggerFactory.getLogger(MemberQueryServiceImpl.class);

    private final RequestContext requestContext;

    public MemberQueryServiceImpl(MemberMapper memberMapper, MemberRepository memberRepository, RequestContext requestContext) {
        this.memberMapper = memberMapper;
        this.memberRepository = memberRepository;
        this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
    }

    @Override
    public void assertExists(String brandUuid, String memberUuid) throws MemberException {
        try {
            Optional<Member> entity = memberRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, memberUuid);
            if (entity.isEmpty()) {
                throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_NOT_FOUND, "Member not found");
            }
        } catch (Exception e) {
            logger.error("Error - brandUuid={}, memberUuid={}", brandUuid, memberUuid, e);
            if (e instanceof MemberException) {
                throw (MemberException) e;
            }
            throw new MemberException(requestContext.getTrackingNumber(), MemberException.FIND_FAILED, e);
        }
    }

    @Override
    public MemberDto find(String brandUuid, String memberUuid) throws MemberException {
        try {
            Optional<Member> entity = memberRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, memberUuid);
            if (entity.isEmpty()) {
                throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_NOT_FOUND, "Member not found");
            }

            return memberMapper.toDto(entity.get());
        } catch (Exception e) {
            logger.error("Error - brandUuid={}, memberUuid={}", brandUuid, memberUuid, e);
            if (e instanceof MemberException) {
                throw (MemberException) e;
            }
            throw new MemberException(requestContext.getTrackingNumber(), MemberException.FIND_FAILED, e);
        }
    }

    @Override
    public Page<MemberSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws MemberException {
        try {
            return memberRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname"),
                    includeInactive);
        } catch (Exception e) {
            logger.error("Error - criteria={}", criteria, e);
            throw new MemberException(requestContext.getTrackingNumber(), MemberException.FIND_FAILED, e);
        }
    }

    @Override
    public Page<MemberDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MemberException {
        try {
            if (includeInactive) {
                return memberRepository.findAllByBrandUuidAndIsDeletedIsFalse(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname"))
                        .map(m -> memberMapper.toDto(m));
            }

            return memberRepository.findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname"))
                    .map(m -> memberMapper.toDto(m));
        } catch (Exception e) {
            logger.error("Error - brandUuid={}", brandUuid, e);
            throw new MemberException(requestContext.getTrackingNumber(), MemberException.FIND_FAILED, e);
        }
    }
}
