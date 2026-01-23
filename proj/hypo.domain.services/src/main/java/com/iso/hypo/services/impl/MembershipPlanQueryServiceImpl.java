package com.iso.hypo.services.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.domain.dto.MembershipPlanDto;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.services.MembershipPlanQueryService;
import com.iso.hypo.services.exception.MembershipPlanException;
import com.iso.hypo.services.mappers.MembershipPlanMapper;

@Service
public class MembershipPlanQueryServiceImpl implements MembershipPlanQueryService {

	private MembershipPlanRepository membershipPlanRepository;

	private MembershipPlanMapper membershipPlanMapper;
	
	public MembershipPlanQueryServiceImpl(MembershipPlanRepository membershipPlanRepository, MembershipPlanMapper membershipPlanMapper) {
		this.membershipPlanRepository = membershipPlanRepository;
		this.membershipPlanMapper = membershipPlanMapper;
	}

	@Override
	public void assertExists(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}
	}

	@Override
	public MembershipPlanDto find(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}

		return membershipPlanMapper.toDto(entity.get());
	}

	@Override
	public Page<MembershipPlanDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws MembershipPlanException {
		Page<MembershipPlan> pageEntities;
		if (includeInactive) {
			pageEntities = membershipPlanRepository.findAllByBrandUuidAndIsDeletedIsFalse(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		} else {
			pageEntities = membershipPlanRepository.findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		}

		return pageEntities.map(e -> membershipPlanMapper.toDto(e));
	}
}