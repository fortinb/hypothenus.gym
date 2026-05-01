package com.iso.hypo.brand.application.usecase.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iso.hypo.brand.application.dto.GymDto;
import com.iso.hypo.brand.application.dto.search.GymSearchDto;
import com.iso.hypo.brand.application.exception.GymException;
import com.iso.hypo.brand.application.mapper.GymDtoMapper;
import com.iso.hypo.brand.application.repository.GymQueryRepository;
import com.iso.hypo.brand.application.usecase.GymQueryService;
import com.iso.hypo.brand.domain.model.Gym;
import com.iso.hypo.brand.domain.repository.GymRepository;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

@Service
public class GymQueryServiceImpl implements GymQueryService {

	private final GymRepository gymRepository;
	private final GymDtoMapper gymMapper;
    private final GymQueryRepository gymQueryRepository;

	private static final Logger logger = LoggerFactory.getLogger(GymQueryServiceImpl.class);

	private final RequestContext requestContext;

	public GymQueryServiceImpl(
			GymDtoMapper gymMapper, 
			GymRepository gymRepository, 
			GymQueryRepository gymQueryRepository,
			RequestContext requestContext) {
		this.gymMapper = gymMapper;
		this.gymRepository = gymRepository;
		this.gymQueryRepository = gymQueryRepository;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public void assertExists(String brandUuid, String gymUuid) throws GymException {
		try {
			Optional<Gym> entity = gymRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, gymUuid);
			if (entity.isEmpty()) {
				throw new GymException(requestContext.getTrackingNumber(), GymException.GYM_NOT_FOUND, "Gym not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}", brandUuid, gymUuid, e);
			if (e instanceof GymException) {
				throw (GymException) e;
			}
			throw new GymException(requestContext.getTrackingNumber(), GymException.FIND_FAILED, e);
		}
	}
	
	@Override
	public GymDto find(String brandUuid, String gymUuid) throws GymException {
		try {
			Optional<Gym> entity = gymRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, gymUuid);
			if (entity.isEmpty()) {
				throw new GymException(requestContext.getTrackingNumber(), GymException.GYM_NOT_FOUND, "Gym not found");
			}

			return gymMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}", brandUuid, gymUuid, e);
			if (e instanceof GymException) {
				throw (GymException) e;
			}
			throw new GymException(requestContext.getTrackingNumber(), GymException.FIND_FAILED, e);
		}
	}

	@Override
	public PageResultDto<GymSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws GymException {
		try {
			PageResult<GymSearchDto> result = gymQueryRepository.searchAutocomplete(criteria,
					PageRequest.of(page, pageSize), includeInactive);
			return PageResultDto.from(result);
		} catch (Exception e) {
			logger.error("Error - criteria={}", criteria, e);
			throw new GymException(requestContext.getTrackingNumber(), GymException.FIND_FAILED, e);
		}
	}

	@Override
	public PageResultDto<GymDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws GymException {
		try {
			PageRequest pageRequest = PageRequest.of(page, pageSize);
			PageResult<GymDto> result = includeInactive
					? gymRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, pageRequest)
							.map(gymMapper::toDto)
					: gymRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, pageRequest)
							.map(gymMapper::toDto);
			return PageResultDto.from(result);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			throw new GymException(requestContext.getTrackingNumber(), GymException.FIND_FAILED, e);
		}
	}
}