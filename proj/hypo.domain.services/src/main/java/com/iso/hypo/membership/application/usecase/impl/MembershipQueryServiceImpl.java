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
import com.iso.hypo.membership.application.dto.MembershipDto;
import com.iso.hypo.membership.application.exception.MembershipException;
import com.iso.hypo.membership.application.mapper.MembershipDtoMapper;
import com.iso.hypo.membership.application.usecase.MembershipQueryService;
import com.iso.hypo.membership.domain.model.Membership;
import com.iso.hypo.membership.domain.model.enumeration.MembershipPlanPeriodEnum;
import com.iso.hypo.membership.domain.repository.MembershipRepository;

@Service
public class MembershipQueryServiceImpl implements MembershipQueryService {

	private final MembershipRepository membershipRepository;

	private final MembershipDtoMapper membershipMapper;

	private final RequestContext requestContext;

	private static final Logger logger = LoggerFactory.getLogger(MembershipQueryServiceImpl.class);

	public MembershipQueryServiceImpl(MembershipDtoMapper membershipMapper, MembershipRepository membershipRepository, RequestContext requestContext) {
		this.membershipMapper = membershipMapper;
		this.membershipRepository = membershipRepository;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public void assertExists(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid,
						membershipUuid);
			if (entity.isEmpty()) {
				throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipUuid={}", brandUuid, membershipUuid, e);

			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.FIND_FAILED, e);
		}
	}

	@Override
	public MembershipDto find(String brandUuid, String membershipUuid) throws MembershipException {
		try {
			Optional<Membership> entity = membershipRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid,
						membershipUuid);
			if (entity.isEmpty()) {
				throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.MEMBERSHIP_NOT_FOUND, "Membership not found");
			}

			return membershipMapper.toDto(entity.get());
		} catch (Exception e) {
			// Single generic logger call for all exception types
			logger.error("Error - brandUuid={}, membershipUuid={}", brandUuid, membershipUuid, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.FIND_FAILED, e);
		}
	}

	@Override
	public PageResultDto<MembershipDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MembershipException {
		try {
			PageRequest pageRequest = PageRequest.of(page, pageSize);
			PageResult<MembershipDto> result = includeInactive
					? membershipRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, pageRequest)
							.map(membershipMapper::toDto)
					: membershipRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, pageRequest)
							.map(membershipMapper::toDto);
			return PageResultDto.from(result);
		} catch (Exception e) {
			// Single generic logger call for all exception types
			logger.error("Error - brandUuid={}", brandUuid, e);
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.FIND_FAILED, e);
		}
	}
	
	@Override
	public boolean isNewMember(String brandUuid, String memberUuid) throws MembershipException {
		try {
			PageRequest pageRequest = PageRequest.of(0, 10);
			PageResult<Membership> result = membershipRepository.findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(brandUuid, memberUuid, pageRequest);
			return result.getContent().isEmpty() ? true : !result.getContent().stream().anyMatch(item -> item.getMembershipPlan().getPeriod() != MembershipPlanPeriodEnum.trial);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, memberUuid={}", brandUuid, memberUuid, e);
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.FIND_FAILED, e);
		}
	}

	@Override
	public Optional<MembershipDto> findByMembershipPlanUuid(String brandUuid, String memberUuid, String membershipPlanUuid)
			throws MembershipException {
		try {
			Optional<Membership> entity = membershipRepository.findByMembershipPlanUuid(brandUuid, memberUuid, membershipPlanUuid);
			if (entity.isEmpty()) {
				return Optional.empty();
			}

			return Optional.of(membershipMapper.toDto(entity.get()));
		} catch (Exception e) {
			// Single generic logger call for all exception types
			logger.error("Error - brandUuid={}, memberUuid={} membershipPlanUuid={}", brandUuid, memberUuid, membershipPlanUuid, e);
			
			if (e instanceof MembershipException) {
				throw (MembershipException) e;
			}
			throw new MembershipException(requestContext.getTrackingNumber(), MembershipException.FIND_FAILED, e);
		}
	}
}