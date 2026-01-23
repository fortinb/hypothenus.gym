package com.iso.hypo.services.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.dto.CoachDto;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.services.CoachQueryService;
import com.iso.hypo.services.exception.CoachException;
import com.iso.hypo.services.mappers.CoachMapper;

@Service
public class CoachQueryServiceImpl implements CoachQueryService {

	private CoachRepository coachRepository;

	private CoachMapper coachMapper;

	@Autowired
	private Logger logger;

	public CoachQueryServiceImpl(CoachRepository coachRepository, CoachMapper coachMapper) {
		this.coachRepository = coachRepository;
		this.coachMapper = coachMapper;
	}

	@Override
	public void assertExists(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		try {
			Optional<Coach> entity = coachRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid,
					gymUuid, coachUuid);
			if (entity.isEmpty()) {
				throw new CoachException(CoachException.COACH_NOT_FOUND, "Coach not found");
			}
		} catch (CoachException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unhandled error", e);
			throw new CoachException(CoachException.FIND_FAILED, e);
		}
	}

	@Override
	public CoachDto find(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		try {
			Optional<Coach> entity = coachRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid,
					gymUuid, coachUuid);
			if (entity.isEmpty()) {
				throw new CoachException(CoachException.COACH_NOT_FOUND, "Coach not found");
			}

			return coachMapper.toDto(entity.get());
		} catch (CoachException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unhandled error", e);
			throw new CoachException(CoachException.FIND_FAILED, e);
		}
	}

	@Override
	public Page<CoachDto> list(String brandUuid, String gymUuid, int page, int pageSize, boolean includeInactive) throws CoachException {
		try {
			if (includeInactive) {
				return coachRepository
						.findAllByBrandUuidAndGymUuidAndIsDeletedIsFalse(brandUuid, gymUuid,
								PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname"))
						.map(c -> coachMapper.toDto(c));
			}

			return coachRepository
					.findAllByBrandUuidAndGymUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, gymUuid,
							PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname"))
					.map(c -> coachMapper.toDto(c));

		} catch (Exception e) {
			logger.error("Unhandled error", e);
			throw new CoachException(CoachException.FIND_FAILED, e);
		}
	}
}
