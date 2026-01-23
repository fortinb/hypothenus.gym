package com.iso.hypo.services.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

	public BrandQueryServiceImpl(BrandRepository brandRepository, BrandMapper brandMapper) {
		this.brandRepository = brandRepository;
		this.brandMapper = brandMapper;
	}

	@Override
	public void assertExists(String brandUuid) throws BrandException {
		Optional<Brand> entity = brandRepository.findByUuidAndIsDeletedIsFalse(brandUuid);
		if (entity.isEmpty()) {
			throw new BrandException(BrandException.BRAND_NOT_FOUND, "Brand not found");
		}
	}

	@Override
	public BrandDto find(String brandUuid)  throws BrandException {
		Optional<Brand> entity = brandRepository.findByUuidAndIsDeletedIsFalse(brandUuid);
		if (entity.isEmpty()) {
			throw new BrandException(BrandException.BRAND_NOT_FOUND, "Brand not found");
		}

		return brandMapper.toDto(entity.get());
	}
	
	@Override
	public Page<BrandSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws BrandException {
		return brandRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),
				includeInactive);
	}

	@Override
	public Page<BrandDto> list(int page, int pageSize, boolean includeInactive) throws BrandException {
		if (includeInactive) {
			return brandRepository.findAllByIsDeletedIsFalse(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
					.map(b -> brandMapper.toDto(b));
		}

		return brandRepository
				.findAllByIsDeletedIsFalseAndIsActiveIsTrue(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
				.map(b -> brandMapper.toDto(b));
	}
}
