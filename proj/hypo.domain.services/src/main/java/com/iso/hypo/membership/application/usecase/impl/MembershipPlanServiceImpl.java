package com.iso.hypo.membership.application.usecase.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.membership.application.dto.MembershipPlanDto;
import com.iso.hypo.membership.application.exception.MembershipPlanException;
import com.iso.hypo.membership.application.mapper.MembershipPlanDtoMapper;
import com.iso.hypo.membership.application.port.BrandServicePort;
import com.iso.hypo.membership.application.port.CourseServicePort;
import com.iso.hypo.membership.application.port.GymServicePort;
import com.iso.hypo.membership.application.port.dto.CourseRef;
import com.iso.hypo.membership.application.port.dto.GymRef;
import com.iso.hypo.membership.application.usecase.MembershipPlanService;
import com.iso.hypo.membership.domain.model.MembershipPlan;
import com.iso.hypo.membership.domain.repository.MembershipPlanRepository;

@Service
public class MembershipPlanServiceImpl implements MembershipPlanService {

	private final BrandServicePort brandServicePort;

	private final GymServicePort gymServicePort;

	private final CourseServicePort courseServicePort;

	private final MembershipPlanRepository membershipPlanRepository;

	private final MembershipPlanDtoMapper membershipPlanMapper;

	private static final Logger logger = LoggerFactory.getLogger(MembershipPlanServiceImpl.class);

	private final RequestContext requestContext;

	public MembershipPlanServiceImpl(MembershipPlanDtoMapper membershipPlanMapper,
			MembershipPlanRepository membershipPlanRepository, BrandServicePort brandServicePort,
			GymServicePort gymServicePort, CourseServicePort courseServicePort, RequestContext requestContext) {
		this.membershipPlanMapper = membershipPlanMapper;
		this.membershipPlanRepository = membershipPlanRepository;
		this.brandServicePort = brandServicePort;
		this.gymServicePort = gymServicePort;
		this.courseServicePort = courseServicePort;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public MembershipPlanDto create(MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		try {
			Assert.notNull(membershipPlanDto, "membershipPlanDto must not be null");
			MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);

			if (!brandServicePort.brandExists(membershipPlan.getBrandUuid())) {
				throw new MembershipPlanException(requestContext.getTrackingNumber(),
						MembershipPlanException.BRAND_NOT_FOUND, "Brand not found");
			}

			membershipPlan.setIncludedGymUuids(
					this.resolveGymReferences(membershipPlan.getBrandUuid(), membershipPlan.getIncludedGymUuids()));
			membershipPlan.setIncludedCourseUuids(this.resolveCourseReferences(membershipPlan.getBrandUuid(),
					membershipPlan.getIncludedCourseUuids()));

			membershipPlan.setCreatedOn(Instant.now());
			membershipPlan.setCreatedBy(requestContext.getUsername());
			membershipPlan.setUuid(UUID.randomUUID().toString());

			MembershipPlan saved = membershipPlanRepository.save(membershipPlan);
			return membershipPlanMapper.toDto(saved);

		} catch (Exception e) {
			logger.error("Error - brandUuid={}", membershipPlanDto.getBrandUuid(), e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(),
					MembershipPlanException.CREATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MembershipPlanDto update(MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		try {
			return updateMembershipPlan(membershipPlanDto, false);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", membershipPlanDto.getBrandUuid(),
					membershipPlanDto.getUuid(), e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.UPDATE_FAILED,
					e);
		}
	}

	@Override
	@Transactional
	public MembershipPlanDto patch(MembershipPlanDto membershipPlanDto) throws MembershipPlanException {
		try {
			return updateMembershipPlan(membershipPlanDto, true);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", membershipPlanDto.getBrandUuid(),
					membershipPlanDto.getUuid(), e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.UPDATE_FAILED,
					e);
		}
	}

	@Override
	@Transactional
	public MembershipPlanDto activate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			MembershipPlan entity = this.readByMembershipPlanUuid(brandUuid, membershipPlanUuid);
			entity.activate(requestContext.getUsername());
			membershipPlanRepository.save(entity);

			return membershipPlanMapper.toDto(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", brandUuid, membershipPlanUuid, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(),
					MembershipPlanException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public MembershipPlanDto deactivate(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			MembershipPlan entity = this.readByMembershipPlanUuid(brandUuid, membershipPlanUuid);
			entity.deactivate(requestContext.getUsername());
			membershipPlanRepository.save(entity);

			return membershipPlanMapper.toDto(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", brandUuid, membershipPlanUuid, e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(),
					MembershipPlanException.DEACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public void delete(String brandUuid, String membershipPlanUuid) throws MembershipPlanException {
		try {
			MembershipPlan entity = this.readByMembershipPlanUuid(brandUuid, membershipPlanUuid);
			entity.delete(requestContext.getUsername());
			membershipPlanRepository.save(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", brandUuid, membershipPlanUuid, e);

			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.DELETE_FAILED,
					e);
		}
	}

	@Override
	public void deleteAllByBrandUuid(String brandUuid) throws MembershipPlanException {
		try {
			long deletedCount = membershipPlanRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());

			logger.info("MembershipPlan deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);
		} catch (Exception e) {
			logger.error("Error - brandId={}", brandUuid, e);

			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.DELETE_FAILED,
					e);
		}
	}

	private MembershipPlanDto updateMembershipPlan(MembershipPlanDto membershipPlanDto, boolean skipNull)
			throws MembershipPlanException {
		try {
			Assert.notNull(membershipPlanDto, "membershipPlanDto must not be null");
			MembershipPlan membershipPlan = membershipPlanMapper.toEntity(membershipPlanDto);

			MembershipPlan oldMembershipPlan = this.readByMembershipPlanUuid(membershipPlan.getBrandUuid(),
					membershipPlan.getUuid());

			membershipPlan.setIncludedGymUuids(
					this.resolveGymReferences(membershipPlan.getBrandUuid(), membershipPlan.getIncludedGymUuids()));
			membershipPlan.setIncludedCourseUuids(this.resolveCourseReferences(membershipPlan.getBrandUuid(),
					membershipPlan.getIncludedCourseUuids()));

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(skipNull).setCollectionsMergeEnabled(false);

			mapper = membershipPlanMapper.initMembershipPlanMappings(mapper);
			mapper.map(membershipPlan, oldMembershipPlan);

			oldMembershipPlan.setModifiedOn(Instant.now());
			oldMembershipPlan.setModifiedBy(requestContext.getUsername());

			MembershipPlan saved = membershipPlanRepository.save(oldMembershipPlan);
			return membershipPlanMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, membershipPlanUuid={}", membershipPlanDto.getBrandUuid(),
					membershipPlanDto.getUuid(), e);
			if (e instanceof MembershipPlanException) {
				throw (MembershipPlanException) e;
			}
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.UPDATE_FAILED,
					e);
		}
	}

	private MembershipPlan readByMembershipPlanUuid(String brandUuid, String membershipPlanUuid)
			throws MembershipPlanException {
		Optional<MembershipPlan> entity = membershipPlanRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid,
				membershipPlanUuid);
		if (entity.isEmpty()) {
			throw new MembershipPlanException(requestContext.getTrackingNumber(),
					MembershipPlanException.MEMBERSHIPPLAN_NOT_FOUND, "MembershipPlan not found");
		}

		return entity.get();
	}

	private List<String> resolveGymReferences(String brandUuid, List<String> gymUuids) throws MembershipPlanException {
		if (gymUuids == null || gymUuids.isEmpty()) {
			return Collections.emptyList();
		}

		for (String gymUuid : gymUuids) {
			Optional<GymRef> entity = gymServicePort.find(brandUuid, gymUuid);
			if (entity.isEmpty()) {
				throw new MembershipPlanException(requestContext.getTrackingNumber(),
						MembershipPlanException.GYM_NOT_FOUND, "Gym not found - gymUuid=" + gymUuid);
			}
		}

		return gymUuids;
	}

	private List<String> resolveCourseReferences(String brandUuid, List<String> courseUuids)
			throws MembershipPlanException {
		if (courseUuids == null || courseUuids.isEmpty()) {
			return Collections.emptyList();
		}
		for (String courseUuid : courseUuids) {
			Optional<CourseRef> entity = courseServicePort.find(brandUuid, courseUuid);
			if (entity.isEmpty()) {
				throw new MembershipPlanException(requestContext.getTrackingNumber(),
						MembershipPlanException.COURSE_NOT_FOUND, "Course not found - courseUuid=" + courseUuid);
			}
		}

		return courseUuids;
	}

	@Override
	public void removeAllGymReferencesByGymUuid(String brandUuid, String gymUuid) throws MembershipPlanException {
		try {
			long modifiedCount = membershipPlanRepository.removeGymReferences(brandUuid, gymUuid);
			logger.info("Gym references removed from membership plans - gymUuid={} modifiedCount={}", gymUuid,
					modifiedCount);
		} catch (Exception e) {
			logger.error("Error removing gym references - gymUuid={}", gymUuid, e);
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.DELETE_FAILED,
					e);
		}
	}

	@Override
	public void removeAllCourseReferencesByCourseUuid(String brandUuid, String courseUuid)
			throws MembershipPlanException {
		try {
			long modifiedCount = membershipPlanRepository.removeCourseReferences(brandUuid, courseUuid);
			logger.info("Course references removed from membership plans - courseUuid={} modifiedCount={}", courseUuid,
					modifiedCount);
		} catch (Exception e) {
			logger.error("Error removing course references - courseUuid={}", courseUuid, e);
			throw new MembershipPlanException(requestContext.getTrackingNumber(), MembershipPlanException.DELETE_FAILED,
					e);
		}
	}
}