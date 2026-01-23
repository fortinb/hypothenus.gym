package com.iso.hypo.services.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.dto.BrandDto;
import com.iso.hypo.domain.dto.BrandSearchDto;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.services.BrandQueryService;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.services.mappers.BrandMapper;

@Service
public class BrandQueryServiceImpl implements BrandQueryService {

	private BrandRepository brandRepository;

	private BrandMapper brandMapper;

	@Autowired
	private Logger logger;

	@Autowired
	private RequestContext requestContext;

	public BrandQueryServiceImpl(BrandRepository brandRepository, BrandMapper brandMapper) {
		this.brandRepository = brandRepository;
		this.brandMapper = brandMapper;
	}

	@Override
	public void assertExists(String brandUuid) throws BrandException {
		try {
			Optional<Brand> entity = brandRepository.findByUuidAndIsDeletedIsFalse(brandUuid);
			if (entity.isEmpty()) {
				throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.BRAND_NOT_FOUND, "Brand not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, trackingNumber={}", brandUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof BrandException) {
				throw (BrandException) e;
			}
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.FIND_FAILED, e);
		}
	}

	@Override
	public BrandDto find(String brandUuid)  throws BrandException {
		try {
			Optional<Brand> entity = brandRepository.findByUuidAndIsDeletedIsFalse(brandUuid);
			if (entity.isEmpty()) {
				throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.BRAND_NOT_FOUND, "Brand not found");
			}

			return brandMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, trackingNumber={}", brandUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof BrandException) {
				throw (BrandException) e;
			}
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.FIND_FAILED, e);
		}
	}
	
	@Override
	public Page<BrandSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws BrandException {
		try {
			return brandRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),
					includeInactive);
		} catch (Exception e) {
			logger.error("Error - criteria={}, trackingNumber={}", criteria, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.FIND_FAILED, e);
		}
	}

	@Override
	public Page<BrandDto> list(int page, int pageSize, boolean includeInactive) throws BrandException {
		try {
			if (includeInactive) {
				return brandRepository.findAllByIsDeletedIsFalse(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
						.map(b -> brandMapper.toDto(b));
			}

			return brandRepository
					.findAllByIsDeletedIsFalseAndIsActiveIsTrue(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
					.map(b -> brandMapper.toDto(b));
		} catch (Exception e) {
			logger.error("Error - page={}, pageSize={}, includeInactive={}, trackingNumber={}", page, pageSize, includeInactive, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.FIND_FAILED, e);
		}
 	}
 }