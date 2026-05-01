package com.iso.hypo.brand.application.usecase.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iso.hypo.brand.application.dto.BrandDto;
import com.iso.hypo.brand.application.dto.search.BrandSearchDto;
import com.iso.hypo.brand.application.exception.BrandException;
import com.iso.hypo.brand.application.mapper.BrandDtoMapper;
import com.iso.hypo.brand.application.repository.BrandQueryRepository;
import com.iso.hypo.brand.application.usecase.BrandQueryService;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.domain.repository.BrandRepository;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

@Service
public class BrandQueryServiceImpl implements BrandQueryService {

	private final BrandRepository brandRepository;
	private final BrandQueryRepository brandQueryRepository;
	private final BrandDtoMapper brandMapper;
	private final RequestContext requestContext;

	private static final Logger logger = LoggerFactory.getLogger(BrandQueryServiceImpl.class);

	public BrandQueryServiceImpl(BrandDtoMapper brandMapper, BrandRepository brandRepository,
			BrandQueryRepository brandQueryRepository, RequestContext requestContext) {
		this.brandMapper = brandMapper;
		this.brandRepository = brandRepository;
		this.brandQueryRepository = brandQueryRepository;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public void assertExists(String brandUuid) throws BrandException {
		try {
			Optional<Brand> entity = brandRepository.findByUuidAndDeletedIsFalse(brandUuid);
			if (entity.isEmpty()) {
				throw new BrandException(requestContext.getTrackingNumber(), BrandException.BRAND_NOT_FOUND,
						"Brand not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			if (e instanceof BrandException)
				throw (BrandException) e;
			throw new BrandException(requestContext.getTrackingNumber(), BrandException.FIND_FAILED, e);
		}
	}

	@Override
	public BrandDto find(String brandUuid) throws BrandException {
		try {
			Optional<Brand> entity = brandRepository.findByUuidAndDeletedIsFalse(brandUuid);
			if (entity.isEmpty()) {
				throw new BrandException(requestContext.getTrackingNumber(), BrandException.BRAND_NOT_FOUND,
						"Brand not found");
			}
			return brandMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			if (e instanceof BrandException)
				throw (BrandException) e;
			throw new BrandException(requestContext.getTrackingNumber(), BrandException.FIND_FAILED, e);
		}
	}

	@Override
	public BrandDto findByCode(String brandCode) throws BrandException {
		try {
			Optional<Brand> entity = brandRepository.findByCodeAndDeletedIsFalse(brandCode);
			if (entity.isEmpty()) {
				throw new BrandException(requestContext.getTrackingNumber(), BrandException.BRAND_NOT_FOUND,
						"Brand not found");
			}
			return brandMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandCode={}", brandCode, e);
			if (e instanceof BrandException)
				throw (BrandException) e;
			throw new BrandException(requestContext.getTrackingNumber(), BrandException.FIND_FAILED, e);
		}
	}

	@Override
	public PageResultDto<BrandSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws BrandException {
		try {
			PageResult<BrandSearchDto> result = brandQueryRepository.searchAutocomplete(criteria,
					PageRequest.of(page, pageSize), includeInactive);
			return PageResultDto.from(result);
		} catch (Exception e) {
			logger.error("Error - criteria={}", criteria, e);
			throw new BrandException(requestContext.getTrackingNumber(), BrandException.FIND_FAILED, e);
		}
	}

	@Override
	public PageResultDto<BrandDto> list(int page, int pageSize, boolean includeInactive) throws BrandException {
		try {
			PageRequest pageRequest = PageRequest.of(page, pageSize);
			PageResult<BrandDto> result = includeInactive
					? brandRepository.findAllByDeletedIsFalse(pageRequest)
							.map(brandMapper::toDto)
					: brandRepository.findAllByDeletedIsFalseAndActiveIsTrue(pageRequest)
							.map(brandMapper::toDto);
			return PageResultDto.from(result);
		} catch (Exception e) {
			logger.error("Error - page={}, pageSize={}, includeInactive={}", page, pageSize, includeInactive, e);
			throw new BrandException(requestContext.getTrackingNumber(), BrandException.FIND_FAILED, e);
		}
	}
}