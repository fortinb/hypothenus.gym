package com.iso.hypo.services.impl;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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

	public GymQueryServiceImpl(BrandRepository brandRepository, GymRepository gymRepository, GymMapper gymMapper) {
		this.gymRepository = gymRepository;
		this.gymMapper = gymMapper;
	}

	@Override
	public void assertExists(String brandUuid, String gymUuid) throws GymException {
		Optional<Gym> entity = gymRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.GYM_NOT_FOUND, "Gym not found");
		}
	}
	
	@Override
	public GymDto find(String brandUuid, String gymUuid) throws GymException {
		Optional<Gym> entity = gymRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.GYM_NOT_FOUND, "Gym not found");
		}

		return gymMapper.toDto(entity.get());
	}

	@Override
	public Page<GymSearchDto> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws GymException {
		return gymRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),
				includeInactive);
	}

	@Override
	public Page<GymDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws GymException {
		if (includeInactive) {
			return gymRepository.findAllByBrandUuidAndIsDeletedIsFalse(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
				.map(g -> gymMapper.toDto(g));
		}

		return gymRepository
				.findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
				.map(g -> gymMapper.toDto(g));
	}
}