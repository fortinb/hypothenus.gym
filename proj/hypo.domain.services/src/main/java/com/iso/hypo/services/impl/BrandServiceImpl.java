package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.dto.BrandDto;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.services.BrandService;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.services.mappers.BrandMapper;

@Service
public class BrandServiceImpl implements BrandService {

	private BrandRepository brandRepository;

	private BrandMapper brandMapper;

	@Autowired
	private RequestContext requestContext;

	@Autowired
	private Logger logger;

	public BrandServiceImpl(BrandRepository brandRepository, BrandMapper brandMapper) {
		this.brandRepository = brandRepository;
		this.brandMapper = brandMapper;
	}

	@Override
	public BrandDto create(BrandDto brandDto) throws BrandException {
		try {
			Brand brand = brandMapper.toEntity(brandDto);
			Optional<Brand> existingBrand = brandRepository.findByCode(brand.getCode());
			if (existingBrand.isPresent()) {
				throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.BRAND_CODE_ALREADY_EXIST, "Duplicate brand code");
			}

			brand.setCreatedOn(Instant.now());
			brand.setCreatedBy(requestContext.getUsername());
			brand.setUuid(UUID.randomUUID().toString());
			
			Brand saved = brandRepository.save(brand);
			return brandMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, trackingNumber={}", brandDto != null ? brandDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof BrandException) {
				throw (BrandException) e;
			}
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.CREATION_FAILED, e);
		}
	}

	@Override
	public BrandDto update(BrandDto brandDto) throws BrandException {
		try {
			Brand brand = brandMapper.toEntity(brandDto);
			Brand oldBrand = this.readByBrandUuid(brand.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration()
				.setSkipNullEnabled(false)
				.setCollectionsMergeEnabled(false);
			
			PropertyMap<Brand, Brand> brandPropertyMap = new PropertyMap<Brand, Brand>() {
				protected void configure() {
					skip().setId(null);
					skip().setActive(false);
				}
			};
			
			mapper.addMappings(brandPropertyMap);
			mapper = brandMapper.initBrandMappings(mapper);
			
			mapper.map(brand, oldBrand);

			oldBrand.setModifiedOn(Instant.now());
			oldBrand.setModifiedBy(requestContext.getUsername());

			Brand saved = brandRepository.save(oldBrand);
			return brandMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, trackingNumber={}", brandDto != null ? brandDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof BrandException) {
				throw (BrandException) e;
			}
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.UPDATE_FAILED, e);
		}
	}

	@Override
	public BrandDto patch(BrandDto brandDto) throws BrandException {
		try {
			Brand brand = brandMapper.toEntity(brandDto);
			Brand oldBrand = this.readByBrandUuid(brand.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(true);

			PropertyMap<Brand, Brand> brandPropertyMap = new PropertyMap<Brand, Brand>() {
				protected void configure() {
					skip().setId(null);
					skip().setContacts(null);
					skip().setPhoneNumbers(null);
				}
			};
			
			mapper.addMappings(brandPropertyMap);
			mapper = brandMapper.initBrandMappings(mapper);
			
			mapper.map(brand, oldBrand);

			oldBrand.setModifiedOn(Instant.now());
			oldBrand.setModifiedBy(requestContext.getUsername());

			Brand saved = brandRepository.save(oldBrand);
			return brandMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, trackingNumber={}", brandDto != null ? brandDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof BrandException) {
				throw (BrandException) e;
			}
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.UPDATE_FAILED, e);
		}
	}

	@Override
	public void delete(String brandUuid) throws BrandException {
		try {
			Brand entity = this.readByBrandUuid(brandUuid);
			entity.setDeleted(true);

			entity.setDeletedOn(Instant.now());
			entity.setDeletedBy(requestContext.getUsername());

			brandRepository.save(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, trackingNumber={}", brandUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof BrandException) {
				throw (BrandException) e;
			}
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.DELETE_FAILED, e);
		}
	}

	@Override
	public BrandDto activate(String brandUuid) throws BrandException {
		try {
			Optional<Brand> entity = brandRepository.activate(brandUuid);
			if (entity.isEmpty()) {
				throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.BRAND_NOT_FOUND, "Brand not found");
			}

			return brandMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, trackingNumber={}", brandUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof BrandException) {
				throw (BrandException) e;
			}
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	public BrandDto deactivate(String brandUuid) throws BrandException {
		try {
			Optional<Brand> entity = brandRepository.deactivate(brandUuid);
			if (entity.isEmpty()) {
				throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.BRAND_NOT_FOUND, "Brand not found");
			}

			return brandMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, trackingNumber={}", brandUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof BrandException) {
				throw (BrandException) e;
			}
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.DEACTIVATION_FAILED, e);
		}
	}
	
	private Brand readByBrandUuid(String brandUuid) throws BrandException {
		Optional<Brand> entity = brandRepository.findByUuidAndIsDeletedIsFalse(brandUuid);
		if (entity.isEmpty()) {
			throw new BrandException(requestContext != null ? requestContext.getTrackingNumber() : null, BrandException.BRAND_NOT_FOUND, "Brand not found");
		}

		return entity.get();
	}
}