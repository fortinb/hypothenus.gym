package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.domain.dto.MembershipPlanDto;
import com.iso.hypo.repositories.MembershipPlanRepository;
import com.iso.hypo.services.BrandQueryService;
import com.iso.hypo.services.MembershipPlanService;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.services.exception.MembershipPlanException;
import com.iso.hypo.services.mappers.MembershipPlanMapper;

@Service
public class MembershipPlanServiceImpl implements MembershipPlanService {

	private BrandQueryService brandQueryService;;

	private MembershipPlanRepository membershipPlanRepository;

	private MembershipPlanMapper membershipPlanMapper;

	@Autowired
	private Logger logger;

	@Autowired
	private RequestContext requestContext;

	public MembershipPlanServiceImpl(BrandQueryService brandQueryService,
							 MembershipPlanRepository membershipPlanRepository, 
							 MembershipPlanMapper membershipPlanMapper) {
		this.brandQueryService = brandQueryService;
		this.membershipPlanRepository = membershipPlanRepository;
		this.membershipPlanMapper = membershipPlanMapper;
	}

	@Override
	public MembershipPlanDto create(String brandUuid, MembershipPlanDto membershipPlanDto)	throws MembershipPlanException {
		try {
			brandQueryService.assertExists(brandUuid);

			MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);
			if (!membershipPlan.getBrandUuid().equals(brandUuid)) {
				throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.INVALID_BRAND, "Invalid brand");
			}

			membershipPlan.setCreatedOn(Instant.now());
			membershipPlan.setCreatedBy(requestContext.getUsername());
			membershipPlan.setUuid(UUID.randomUUID().toString());

			MembershipPlan saved = membershipPlanRepository.save(membershipPlan);
			return membershipPlanMapper.toDto(saved);

		} catch (Exception e) {
			logger.error("Error - brandUuid={}, trackingNumber={}", brandUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof BrandException) {
				throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.BRAND_NOT_FOUND, "Brand not found");
			}
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.CREATION_FAILED, e);
		}
	}

	@Override
	public MembershipPlanDto update(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		try {
			MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);
			if (!membershipPlan.getBrandUuid().equals(brandUuid)) {
				throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.INVALID_BRAND, "Invalid brand");
			}

			MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(brandUuid, membershipPlan.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(false);

			mapper = membershipPlanMapper.initMembershipPlanMappings(mapper);
			mapper.map(membershipPlan, oldMembershipPlan);

			oldMembershipPlan.setModifiedOn(Instant.now());
			oldMembershipPlan.setModifiedBy(requestContext.getUsername());

			MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
			return membershipPlanMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}, trackingNumber={}", brandUuid, membershipPlanDto != null ? membershipPlanDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.UPDATE_FAILED, e);
		}
	}

	@Override
	public MembershipPlanDto patch(String brandUuid, MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		try {
			MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);
			if (!membershipPlan.getBrandUuid().equals(brandUuid)) {
				throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.INVALID_BRAND, "Invalid brand");
			}

			MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(brandUuid, membershipPlan.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(true);

			mapper = membershipPlanMapper.initMembershipPlanMappings(mapper);
			mapper.map(membershipPlan, oldMembershipPlan);

			oldMembershipPlan.setModifiedOn(Instant.now());
			oldMembershipPlan.setModifiedBy(requestContext.getUsername());

			MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
			return membershipPlanMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}, trackingNumber={}", brandUuid, membershipPlanDto != null ? membershipPlanDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.UPDATE_FAILED, e);
		}
	}

	@Override
	public void delete(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			MembershipPlan entity = this.readByMembershipPlanUuid(brandUuid, membershipPlanUuid);
			entity.setDeleted(true);

			entity.setDeletedOn(Instant.now());
			entity.setDeletedBy(requestContext.getUsername());

			membershipPlanRepository.save(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}, trackingNumber={}", brandUuid, membershipPlanUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.DELETE_FAILED, e);
		}
	}

	@Override
	public MembershipPlanDto activate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			Optional<MembershipPlan> entity = membershipPlanRepository.activate(brandUuid, membershipPlanUuid);
			if (entity.isEmpty()) {
				throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND,
						"MembershipPlan not found");
			}

			return membershipPlanMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}, trackingNumber={}", brandUuid, membershipPlanUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	public MembershipPlanDto deactivate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			Optional<MembershipPlan> entity = membershipPlanRepository.deactivate(brandUuid, membershipPlanUuid);
			if (entity.isEmpty()) {
				throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND,
						"MembershipPlan not found");
			}

			return membershipPlanMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}, trackingNumber={}", brandUuid, membershipPlanUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.DEACTIVATION_FAILED, e);
		}
	}

	private MembershipPlan readByMembershipPlanUuid(String brandUuid, String membershipPlanUuid)
			throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid,
					membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(requestContext != null ? requestContext.getTrackingNumber() : null, MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND,
					"MembershipPlan not found");
		}

		return entity.get();
	}
}
