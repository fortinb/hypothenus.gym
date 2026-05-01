package com.iso.hypo.membership.application.usecase.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.membership.application.dto.MemberDto;
import com.iso.hypo.membership.application.dto.search.MemberSearchDto;
import com.iso.hypo.membership.application.exception.MemberException;
import com.iso.hypo.membership.application.mapper.MemberDtoMapper;
import com.iso.hypo.membership.application.port.UserServicePort;
import com.iso.hypo.membership.application.port.dto.UserRef;
import com.iso.hypo.membership.application.repository.MemberQueryRepository;
import com.iso.hypo.membership.application.usecase.MemberQueryService;
import com.iso.hypo.membership.domain.model.Member;
import com.iso.hypo.membership.domain.repository.MemberRepository;

@Service
public class MemberQueryServiceImpl implements MemberQueryService {

	private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;

    private final UserServicePort userServicePort;

    private final MemberDtoMapper memberMapper;

    private static final Logger logger = LoggerFactory.getLogger(MemberQueryServiceImpl.class);

    private final RequestContext requestContext;

    public MemberQueryServiceImpl(
    		MemberDtoMapper memberMapper, 
    		MemberRepository memberRepository,
    		MemberQueryRepository memberQueryRepository, 
    		UserServicePort brandUserPort, 
    		RequestContext requestContext) {
        this.memberMapper = memberMapper;
        this.memberRepository = memberRepository;
        this.memberQueryRepository = memberQueryRepository;
        this.userServicePort = Objects.requireNonNull(brandUserPort, "brandUserPort must not be null");
        this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
    }

    @Override
    public void assertExists(String brandUuid, String memberUuid) throws MemberException {
        try {
            Optional<Member> entity = memberRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid);
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
            Optional<Member> entity = memberRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, memberUuid);
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
    public PageResultDto<MemberSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive) throws MemberException {
        try {
        	PageResult<MemberSearchDto> result = memberQueryRepository.searchAutocomplete(criteria,
					PageRequest.of(page, pageSize), includeInactive);
			return PageResultDto.from(result);
        } catch (Exception e) {
            logger.error("Error - criteria={}", criteria, e);
            throw new MemberException(requestContext.getTrackingNumber(), MemberException.FIND_FAILED, e);
        }
    }

    @Override
    public PageResultDto<MemberDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MemberException {
        try {
			PageRequest pageRequest = PageRequest.of(page, pageSize);
			PageResult<MemberDto> result = includeInactive
					? memberRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, pageRequest)
							.map(memberMapper::toDto)
					: memberRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, pageRequest)
							.map(memberMapper::toDto);
			return PageResultDto.from(result);
          } catch (Exception e) {
            logger.error("Error - brandUuid={}", brandUuid, e);
            throw new MemberException(requestContext.getTrackingNumber(), MemberException.FIND_FAILED, e);
        }
    }

    @Override
    public MemberDto findByUserIdpId(String brandUuid, String idpId) throws MemberException {
        try {
            Optional<UserRef> user = userServicePort.findByIdpId(idpId);
            if (user.isEmpty()) {
                throw new MemberException(requestContext.getTrackingNumber(), MemberException.USER_NOT_FOUND, "User not found for idpId: " + idpId);
            }

            Optional<Member> member = memberRepository.findByBrandUuidAndUserUuidAndDeletedIsFalse(brandUuid, user.get().getUuid());
            if (member.isEmpty()) {
                throw new MemberException(requestContext.getTrackingNumber(), MemberException.MEMBER_NOT_FOUND, "Member not found for user with idpId: " + idpId);
            }

            return memberMapper.toDto(member.get());
        } catch (Exception e) {
            logger.error("Error - brandUuid={}, idpId={}", brandUuid, idpId, e);
            if (e instanceof MemberException) {
                throw (MemberException) e;
            }
            throw new MemberException(requestContext.getTrackingNumber(), MemberException.FIND_FAILED, e);
        }
    }
}