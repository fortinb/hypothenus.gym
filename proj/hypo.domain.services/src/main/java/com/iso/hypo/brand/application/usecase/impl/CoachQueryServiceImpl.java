package com.iso.hypo.brand.application.usecase.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iso.hypo.brand.application.dto.CoachDto;
import com.iso.hypo.brand.application.exception.CoachException;
import com.iso.hypo.brand.application.mapper.CoachDtoMapper;
import com.iso.hypo.brand.application.usecase.CoachQueryService;
import com.iso.hypo.brand.domain.model.Coach;
import com.iso.hypo.brand.domain.repository.CoachRepository;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

@Service
public class CoachQueryServiceImpl implements CoachQueryService {

	private final CoachRepository coachRepository;

	private final CoachDtoMapper coachMapper;

	private static final Logger logger = LoggerFactory.getLogger(CoachQueryServiceImpl.class);

	private final RequestContext requestContext;

	public CoachQueryServiceImpl(CoachDtoMapper coachMapper, CoachRepository coachRepository,
			RequestContext requestContext) {
		this.coachMapper = coachMapper;
		this.coachRepository = coachRepository;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public void assertExists(String brandUuid, String coachUuid) throws CoachException {
		try {
			Optional<Coach> entity = coachRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, coachUuid);
			if (entity.isEmpty()) {
				throw new CoachException(requestContext.getTrackingNumber(), CoachException.COACH_NOT_FOUND,
						"Coach not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, coachUuid={}", brandUuid, coachUuid, e);

			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.FIND_FAILED, e);
		}
	}

	@Override
	public CoachDto find(String brandUuid, String coachUuid) throws CoachException {
		try {
			Optional<Coach> entity = coachRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, coachUuid);
			if (entity.isEmpty()) {
				throw new CoachException(requestContext.getTrackingNumber(), CoachException.COACH_NOT_FOUND,
						"Coach not found");
			}

			return coachMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, coachUuid={}", brandUuid, coachUuid, e);

			if (e instanceof CoachException) {
				throw (CoachException) e;
			}
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.FIND_FAILED, e);
		}
	}

	@Override
	public PageResultDto<CoachDto> list(String brandUuid, int page, int pageSize, boolean includeInactive)
			throws CoachException {
		try {
			PageRequest pageRequest = PageRequest.of(page, pageSize);
			PageResult<CoachDto> result = includeInactive
					? coachRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, pageRequest)
							.map(coachMapper::toDto)
					: coachRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, pageRequest)
							.map(coachMapper::toDto);
			return PageResultDto.from(result);

		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			throw new CoachException(requestContext.getTrackingNumber(), CoachException.FIND_FAILED, e);
		}
	}
}