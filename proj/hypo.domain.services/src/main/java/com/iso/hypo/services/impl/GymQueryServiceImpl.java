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
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.dto.GymDto;
import com.iso.hypo.domain.dto.GymSearchDto;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.services.GymQueryService;
import com.iso.hypo.services.exception.GymException;
import com.iso.hypo.services.mappers.GymMapper;

@Service
public class GymQueryServiceImpl implements GymQueryService {

	private GymRepository gymRepository;

	private GymMapper gymMapper;

	private static final Logger logger = LoggerFactory.getLogger(GymQueryServiceImpl.class);

	private final RequestContext requestContext;

	public GymQueryServiceImpl(BrandRepository brandRepository, GymRepository gymRepository, GymMapper gymMapper, RequestContext requestContext) {
		this.gymRepository = gymRepository;
		this.gymMapper = gymMapper;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public void assertExists(String brandUuid, String gymUuid) throws GymException {
		try {
			Optional<Gym> entity = gymRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid);
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
			Optional<Gym> entity = gymRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid);
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
	public Page<GymSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws GymException {
		try {
			return gymRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),
						includeInactive);
		} catch (Exception e) {
			logger.error("Error - criteria={}", criteria, e);
			throw new GymException(requestContext.getTrackingNumber(), GymException.FIND_FAILED, e);
		}
	}

	@Override
	public Page<GymDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws GymException {
		try {
			if (includeInactive) {
				return gymRepository.findAllByBrandUuidAndIsDeletedIsFalse(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
					.map(g -> gymMapper.toDto(g));
			}

			return gymRepository.findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
					.map(g -> gymMapper.toDto(g));
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			throw new GymException(requestContext.getTrackingNumber(), GymException.FIND_FAILED, e);
		}
	}
}