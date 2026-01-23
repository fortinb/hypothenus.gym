package com.iso.hypo.services.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private Logger logger;

	@Autowired
	private RequestContext requestContext;

	public GymQueryServiceImpl(BrandRepository brandRepository, GymRepository gymRepository, GymMapper gymMapper) {
		this.gymRepository = gymRepository;
		this.gymMapper = gymMapper;
	}

	@Override
	public void assertExists(String brandUuid, String gymUuid) throws GymException {
		try {
			Optional<Gym> entity = gymRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid);
			if (entity.isEmpty()) {
				throw new GymException(requestContext != null ? requestContext.getTrackingNumber() : null, GymException.GYM_NOT_FOUND, "Gym not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, trackingNumber={}", brandUuid, gymUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof GymException) {
				throw (GymException) e;
			}
			throw new GymException(requestContext != null ? requestContext.getTrackingNumber() : null, GymException.FIND_FAILED, e);
		}
	}
	
	@Override
	public GymDto find(String brandUuid, String gymUuid) throws GymException {
		try {
			Optional<Gym> entity = gymRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid);
			if (entity.isEmpty()) {
				throw new GymException(requestContext != null ? requestContext.getTrackingNumber() : null, GymException.GYM_NOT_FOUND, "Gym not found");
			}

			return gymMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, trackingNumber={}", brandUuid, gymUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof GymException) {
				throw (GymException) e;
			}
			throw new GymException(requestContext != null ? requestContext.getTrackingNumber() : null, GymException.FIND_FAILED, e);
		}
	}

	@Override
	public Page<GymSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws GymException {
		try {
			return gymRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),
					includeInactive);
		} catch (Exception e) {
			logger.error("Error - criteria={}, trackingNumber={}", criteria, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			throw new GymException(requestContext != null ? requestContext.getTrackingNumber() : null, GymException.FIND_FAILED, e);
		}
	}

	@Override
	public Page<GymDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws GymException {
		try {
			if (includeInactive) {
				return gymRepository.findAllByBrandUuidAndIsDeletedIsFalse(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
					.map(g -> gymMapper.toDto(g));
			}

			return gymRepository
					.findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
					.map(g -> gymMapper.toDto(g));
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, trackingNumber={}", brandUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			throw new GymException(requestContext != null ? requestContext.getTrackingNumber() : null, GymException.FIND_FAILED, e);
		}
	}
}