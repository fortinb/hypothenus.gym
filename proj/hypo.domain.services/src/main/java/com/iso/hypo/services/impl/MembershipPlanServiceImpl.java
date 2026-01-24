package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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

	private static final Logger logger = LoggerFactory.getLogger(MembershipPlanServiceImpl.class);

	private final RequestContext requestContext;

	public MembershipPlanServiceImpl(BrandQueryService brandQueryService,
							 MembershipPlanRepository membershipPlanRepository, 
							 MembershipPlanMapper membershipPlanMapper,
							 RequestContext requestContext) {
		this.brandQueryService = brandQueryService;
		this.membershipPlanRepository = membershipPlanRepository;
		this.membershipPlanMapper = membershipPlanMapper;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public MembershipPlanDto create(MembershipPlanDto membershipPlanDto)	throws MembershipPlanException {
		try {
			Assert.notNull(membershipPlanDto, "membershipPlanDto must not be null");
			brandQueryService.assertExists(membershipPlanDto.getBrandUuid());

			MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);

			membershipPlan.setCreatedOn(Instant.now());
			membershipPlan.setCreatedBy(requestContext.getUsername());
			membershipPlan.setUuid(UUID.randomUUID().toString());

			MembershipPlan saved = membershipPlanRepository.save(membershipPlan);
			return membershipPlanMapper.toDto(saved);

		} catch (Exception e) {
			logger.error("Error - brandUuid={}", membershipPlanDto.getBrandUuid(), e);
			if (e instanceof BrandException) {
				throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.BRAND_NOT_FOUND, "Brand not found");
			}
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.CREATION_FAILED, e);
		}
	}

	@Override
	public MembershipPlanDto update(MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		try {
			Assert.notNull(membershipPlanDto, "membershipPlanDto must not be null");
			MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);

			MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(membershipPlan.getBrandUuid(), membershipPlan.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(false);

			mapper = membershipPlanMapper.initMembershipPlanMappings(mapper);
			mapper.map(membershipPlan, oldMembershipPlan);

			oldMembershipPlan.setModifiedOn(Instant.now());
			oldMembershipPlan.setModifiedBy(requestContext.getUsername());

			MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
			return membershipPlanMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", membershipPlanDto.getBrandUuid(), membershipPlanDto.getUuid(), e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.UPDATE_FAILED, e);
		}
	}

	@Override
	public MembershipPlanDto patch(MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		try {
			Assert.notNull(membershipPlanDto, "membershipPlanDto must not be null");
			MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);

			MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(membershipPlan.getBrandUuid(), membershipPlan.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(true);

			mapper = membershipPlanMapper.initMembershipPlanMappings(mapper);
			mapper.map(membershipPlan, oldMembershipPlan);

			oldMembershipPlan.setModifiedOn(Instant.now());
			oldMembershipPlan.setModifiedBy(requestContext.getUsername());

			MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
			return membershipPlanMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", membershipPlanDto.getBrandUuid(), membershipPlanDto.getUuid(), e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.UPDATE_FAILED, e);
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
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", brandUuid, membershipPlanUuid, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.DELETE_FAILED, e);
		}
	}

	@Override
	public MembershipPlanDto activate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			Optional<MembershipPlan> entity = membershipPlanRepository.activate(brandUuid, membershipPlanUuid);
			if (entity.isEmpty()) {
				throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND,
						"MembershipPlan not found");
			}

			return membershipPlanMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", brandUuid, membershipPlanUuid, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	public MembershipPlanDto deactivate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			Optional<MembershipPlan> entity = membershipPlanRepository.deactivate(brandUuid, membershipPlanUuid);
			if (entity.isEmpty()) {
				throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND,
						"MembershipPlan not found");
			}

			return membershipPlanMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", brandUuid, membershipPlanUuid, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.DEACTIVATION_FAILED, e);
		}
	}

	private MembershipPlan readByMembershipPlanUuid(String brandUuid, String membershipPlanUuid)
			throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid,
					membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND,
					"MembershipPlan not found");
		}

		return entity.get();
	}
}
