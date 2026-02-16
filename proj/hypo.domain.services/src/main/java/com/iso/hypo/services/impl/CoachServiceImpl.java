package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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

	private final GymQueryService gymQueryService;

	private final CoachRepository coachRepository;

	private final CoachMapper coachMapper;

	private static final Logger logger = LoggerFactory.getLogger(CoachServiceImpl.class);

	private final RequestContext requestContext;

	public CoachServiceImpl(CoachMapper coachMapper, CoachRepository coachRepository, GymQueryService gymQueryService, RequestContext requestContext) {
		this.coachMapper = coachMapper;
		this.coachRepository = coachRepository;
		this.gymQueryService = gymQueryService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public CoachDto create(CoachDto coachDto) throws CoachException {
		try {
			Assert.notNull(coachDto, "coachDto must not be null");
			gymQueryService.assertExists(coachDto.getBrandUuid(), coachDto.getGymUuid());

			Coach coach = coachMapper.toEntity(coachDto);

			coach.setUuid(UUID.randomUUID().toString());
			coach.setCreatedOn(Instant.now());
			coach.setCreatedBy(requestContext.getUsername());

			Coach saved = coachRepository.save(coach);
			return coachMapper.toDto(saved);

		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}", coachDto.getBrandUuid(), coachDto.getGymUuid(), coachDto.getUuid(), e);
			
			if (e instanceof GymException) {
				throw new CoachException(requestContext.getTrackingNumber(), CoachException.GYM_NOT_FOUND, "Gym not found");
			}
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.CREATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public CoachDto update(CoachDto coachDto) throws CoachException {
		try {
			return updateCoach(coachDto, false);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}", coachDto.getBrandUuid(), coachDto.getGymUuid(), coachDto.getUuid(), e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public CoachDto patch(CoachDto coachDto) throws CoachException {
		try {
			return updateCoach(coachDto, true);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}", coachDto.getBrandUuid(), coachDto.getGymUuid(), coachDto.getUuid(), e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public CoachDto activate(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		try {
			Optional<Coach> entity = coachRepository.activate(brandUuid, gymUuid, coachUuid);
			if (entity.isEmpty()) {
				throw new CoachException(requestContext.getTrackingNumber(), CoachException.COACH_NOT_FOUND, "Coach not found");
			}

			return coachMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}", brandUuid, gymUuid, coachUuid, e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public CoachDto deactivate(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		try {
			Optional<Coach> entity = coachRepository.deactivate(brandUuid, gymUuid, coachUuid);
			if (entity.isEmpty()) {
				throw new CoachException(requestContext.getTrackingNumber(), CoachException.COACH_NOT_FOUND, "Coach not found");
			}
		
			return coachMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}", brandUuid, gymUuid, coachUuid, e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.DEACTIVATION_FAILED, e);
		}
	}
	
	@Override
	@Transactional
	public void delete(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		try {
			Coach entity = this.readByCoachUuid(brandUuid, gymUuid, coachUuid);
			coachRepository.delete(entity.getBrandUuid(), entity.getGymUuid(), entity.getUuid(), requestContext.getUsername());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}", brandUuid, gymUuid, coachUuid, e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.DELETE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public void deleteAllByBrandUuid(String brandUuid) throws CoachException {
		try {
			long deletedCount = coachRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());
			
			logger.info("Coach deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.DELETE_FAILED, e);
		}
	}

	@Override
	public void deleteAllByGymUuid(String brandUuid, String gymUuid) throws CoachException {
		try {
			long deletedCount = coachRepository.deleteAllByGymUuid(brandUuid, gymUuid, requestContext.getUsername());
			
			logger.info("Coach deleted for gym - brandUuid={} gymUuid={} deletedCount={} ", brandUuid, gymUuid, deletedCount);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}", brandUuid, gymUuid, e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.DELETE_FAILED, e);
		}
	}
	
	private CoachDto updateCoach(CoachDto coachDto, boolean skipNull) throws CoachException {
		try {
			Assert.notNull(coachDto, "coachDto must not be null");
			Coach coach = coachMapper.toEntity(coachDto);
			
			Coach oldCoach = this.readByCoachUuid(coach.getBrandUuid(), coach.getGymUuid(), coach.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(skipNull).setCollectionsMergeEnabled(false);;

			mapper = coachMapper.initCoachMappings(mapper);
			mapper.map(coach, oldCoach);

			oldCoach.setModifiedOn(Instant.now());
			oldCoach.setModifiedBy(requestContext.getUsername());

			Coach saved = coachRepository.save(oldCoach);
			return coachMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, coachUuid={}", coachDto.getBrandUuid(), coachDto.getGymUuid(), coachDto.getUuid(), e);
			
			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.UPDATE_FAILED, e);
		}
	}
	private Coach readByCoachUuid(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		Optional<Coach> entity = coachRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid,
						coachUuid);
		if (entity.isEmpty()) {
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.COACH_NOT_FOUND, "Coach not found");
		}

		return entity.get();
	}
}