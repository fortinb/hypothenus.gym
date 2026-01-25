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
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.domain.dto.MembershipPlanDto;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.services.MembershipPlanQueryService;
import com.iso.hypo.services.exception.MembershipPlanException;
import com.iso.hypo.services.mappers.MembershipPlanMapper;

@Service
public class MembershipPlanQueryServiceImpl implements MembershipPlanQueryService {

	private final MembershipPlanRepository membershipPlanRepository;

	private final MembershipPlanMapper membershipPlanMapper;
	
	private final RequestContext requestContext;
	
	private static final Logger logger = LoggerFactory.getLogger(MembershipPlanQueryServiceImpl.class);
	
	public MembershipPlanQueryServiceImpl(MembershipPlanMapper membershipPlanMapper, MembershipPlanRepository membershipPlanRepository, RequestContext requestContext) {
		this.membershipPlanMapper = membershipPlanMapper;
		this.membershipPlanRepository = membershipPlanRepository;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public void assertExists(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipPlanUuid);
			if (entity.isEmpty()) {
				throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", brandUuid, membershipPlanUuid, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.FIND_FAILED, e);
		}
	}

	@Override
	public MembershipPlanDto find(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipPlanUuid);
			if (entity.isEmpty()) {
				throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
			}
 
			return membershipPlanMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", brandUuid, membershipPlanUuid, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.FIND_FAILED, e);
		}
	}

	@Override
	public Page<MembershipPlanDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MembershipPlanException {
		try {
			Page<MembershipPlan> pageEntities;
			if (includeInactive) {
				pageEntities = membershipPlanRepository.findAllByBrandUuidAndIsDeletedIsFalse(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
			} else {
				pageEntities = membershipPlanRepository.findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
			}

			return pageEntities.map(e -> membershipPlanMapper.toDto(e));
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.FIND_FAILED, e);
		}
    	}
}