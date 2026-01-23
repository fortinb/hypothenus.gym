package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.dto.CoachDto;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.services.CoachService;
import com.iso.hypo.services.GymQueryService;
import com.iso.hypo.services.exception.CoachException;
import com.iso.hypo.services.exception.GymException;
import com.iso.hypo.services.mappers.CoachMapper;

@Service
public class CoachServiceImpl implements CoachService {

	private GymQueryService gymQueryService;

	private CoachRepository coachRepository;

	private CoachMapper coachMapper;

	@Autowired
	private Logger logger;

	@Autowired
	private RequestContext requestContext;

	public CoachServiceImpl(GymQueryService gymQueryService, CoachRepository coachRepository, CoachMapper coachMapper) {
		this.gymQueryService = gymQueryService;
		this.coachRepository = coachRepository;
		this.coachMapper = coachMapper;
	}

	@Override
	public CoachDto create(String brandUuid, String gymUuid, CoachDto coachDto) throws CoachException {
		try {
			gymQueryService.assertExists(brandUuid, gymUuid);

			Coach coach = coachMapper.toEntity(coachDto);

			if (!coach.getBrandUuid().equals(brandUuid)) {
				throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.INVALID_BRAND, "Invalid brand");
			}

			if (!coach.getGymUuid().equals(gymUuid)) {
				throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.INVALID_GYM, "Invalid gym");
			}

			coach.setUuid(UUID.randomUUID().toString());
			coach.setCreatedOn(Instant.now());
			coach.setCreatedBy(requestContext.getUsername());

			Coach saved = coachRepository.save(coach);
			return coachMapper.toDto(saved);

		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}, trackingNumber={}", brandUuid, gymUuid, coachDto != null ? coachDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof GymException) {
				throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.GYM_NOT_FOUND, "Gym not found");
			}
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.CREATION_FAILED, e);
		}
	}

	@Override
	public CoachDto update(String brandUuid, String gymUuid, CoachDto coachDto) throws CoachException {
		try {
			Coach coach = coachMapper.toEntity(coachDto);
			if (!coach.getBrandUuid().equals(brandUuid)) {
				throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.INVALID_BRAND, "Invalid brand");
			}

			if (!coach.getGymUuid().equals(gymUuid)) {
				throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.INVALID_GYM, "Invalid gym");
			}

			Coach oldCoach = this.readByCoachUuid(brandUuid, gymUuid, coach.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(false);

			mapper = coachMapper.initCoachMappings(mapper);
			mapper.map(coach, oldCoach);

			oldCoach.setModifiedOn(Instant.now());
			oldCoach.setModifiedBy(requestContext.getUsername());

			Coach saved = coachRepository.save(oldCoach);
			return coachMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}, trackingNumber={}", brandUuid, gymUuid, coachDto != null ? coachDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.UPDATE_FAILED, e);
		}
	}

	@Override
	public CoachDto patch(String brandUuid, String gymUuid, CoachDto coachDto) throws CoachException {
		try {
			Coach coach = coachMapper.toEntity(coachDto);
			if (!coach.getGymUuid().equals(gymUuid)) {
				throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.INVALID_BRAND, "Invalid gym");
			}

			Coach oldCoach = this.readByCoachUuid(brandUuid, gymUuid, coach.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(true);

			mapper = coachMapper.initCoachMappings(mapper);
			mapper.map(coach, oldCoach);

			oldCoach.setModifiedOn(Instant.now());
			oldCoach.setModifiedBy(requestContext.getUsername());

			Coach saved = coachRepository.save(oldCoach);
			return coachMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}, trackingNumber={}", brandUuid, gymUuid, coachDto != null ? coachDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.UPDATE_FAILED, e);
		}
	}

	@Override
	public void delete(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		try {
			Coach entity = this.readByCoachUuid(brandUuid, gymUuid, coachUuid);
			entity.setDeleted(true);

			entity.setDeletedOn(Instant.now());
			entity.setDeletedBy(requestContext.getUsername());

			coachRepository.save(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}, trackingNumber={}", brandUuid, gymUuid, coachUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.DELETE_FAILED, e);
		}
	}

	@Override
	public CoachDto activate(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		try {
			Optional<Coach> entity = coachRepository.activate(brandUuid, gymUuid, coachUuid);
			if (entity.isEmpty()) {
				throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.COACH_NOT_FOUND, "Coach not found");
			}

			return coachMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}, trackingNumber={}", brandUuid, gymUuid, coachUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	public CoachDto deactivate(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		try {
			Optional<Coach> entity = coachRepository.deactivate(brandUuid, gymUuid, coachUuid);
			if (entity.isEmpty()) {
				throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.COACH_NOT_FOUND, "Coach not found");
			}
	
			return coachMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}, trackingNumber={}", brandUuid, gymUuid, coachUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.DEACTIVATION_FAILED, e);
		}
	}

	private Coach readByCoachUuid(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		Optional<Coach> entity = coachRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid,
				coachUuid);
		if (entity.isEmpty()) {
			throw new CoachException(requestContext != null ? requestContext.getTrackingNumber() : null, CoachException.COACH_NOT_FOUND, "Coach not found");
		}

		return entity.get();
	}
}