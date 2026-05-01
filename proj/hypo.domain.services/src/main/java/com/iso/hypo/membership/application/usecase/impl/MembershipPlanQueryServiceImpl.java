package com.iso.hypo.membership.application.usecase.impl;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.membership.application.dto.MembershipPlanDto;
import com.iso.hypo.membership.application.exception.MembershipPlanException;
import com.iso.hypo.membership.application.mapper.MembershipPlanDtoMapper;
import com.iso.hypo.membership.application.usecase.MembershipPlanQueryService;
import com.iso.hypo.membership.domain.model.MembershipPlan;
import com.iso.hypo.membership.domain.repository.MembershipPlanRepository;

@Service
public class MembershipPlanQueryServiceImpl implements MembershipPlanQueryService {

	private final MembershipPlanRepository membershipPlanRepository;

	private final MembershipPlanDtoMapper membershipPlanMapper;

	private final RequestContext requestContext;

	private static final Logger logger = LoggerFactory.getLogger(MembershipPlanQueryServiceImpl.class);

	public MembershipPlanQueryServiceImpl(MembershipPlanDtoMapper membershipPlanMapper,
			MembershipPlanRepository membershipPlanRepository, RequestContext requestContext) {
		this.membershipPlanMapper = membershipPlanMapper;
		this.membershipPlanRepository = membershipPlanRepository;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public void assertExists(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			Optional<MembershipPlan> entity = membershipPlanRepository
					.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, membershipPlanUuid);
			if (entity.isEmpty()) {
				throw new MembershipPlanException(requestContext.getTrackingNumber(),
						MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", brandUuid, membershipPlanUuid, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.FIND_FAILED,
					e);
		}
	}

	@Override
	public MembershipPlanDto find(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			Optional<MembershipPlan> entity = membershipPlanRepository
					.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, membershipPlanUuid);
			if (entity.isEmpty()) {
				throw new MembershipPlanException(requestContext.getTrackingNumber(),
						MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
			}

			return membershipPlanMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", brandUuid, membershipPlanUuid, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.FIND_FAILED,
					e);
		}
	}

	@Override
	public PageResultDto<MembershipPlanDto> list(String brandUuid, Date currentDate, int page, int pageSize,
			boolean includeInactive) throws MembershipPlanException {
		try {
			PageResult<MembershipPlanDto> result;
			PageRequest pageRequest = PageRequest.of(page, pageSize);

			if (currentDate == null) {
				result = includeInactive
						? membershipPlanRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, pageRequest)
								.map(membershipPlanMapper::toDto)
						: membershipPlanRepository
								.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, pageRequest)
								.map(membershipPlanMapper::toDto);

			} else { // If currentDate is not null, return plans that are active on the currentDate.
				result = membershipPlanRepository.findActiveOnDate(brandUuid, currentDate, pageRequest)
						.map(membershipPlanMapper::toDto);
			}
			return PageResultDto.from(result);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.FIND_FAILED,
					e);
		}
	}
}