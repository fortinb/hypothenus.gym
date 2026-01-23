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
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.dto.GymDto;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.services.BrandQueryService;
import com.iso.hypo.services.GymService;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.services.exception.GymException;
import com.iso.hypo.services.mappers.GymMapper;

@Service
public class GymServiceImpl implements GymService {

	private BrandQueryService brandQueryService;
	
	private GymRepository gymRepository;

	private GymMapper gymMapper;
	
	@Autowired
	private Logger logger;

	@Autowired
	private RequestContext requestContext;

	public GymServiceImpl(BrandQueryService brandQueryService, GymRepository gymRepository, GymMapper gymMapper) {
		this.brandQueryService = brandQueryService;
		this.gymRepository = gymRepository;
		this.gymMapper = gymMapper;
	}

	@Override
	public GymDto create(GymDto gymDto) throws GymException {
		try {
			brandQueryService.assertExists(gymDto.getBrandUuid());
		
			Gym gym = gymMapper.toEntity(gymDto);
			Optional<Gym> existingGym = gymRepository.findByBrandUuidAndCode(gym.getBrandUuid(), gym.getCode());
			if (existingGym.isPresent()) {
				throw new GymException(GymException.GYM_CODE_ALREADY_EXIST, "Duplicate gym code");
			}
	
			gym.setCreatedOn(Instant.now());
			gym.setCreatedBy(requestContext.getUsername());
			gym.setUuid(UUID.randomUUID().toString());
			
			Gym saved = gymRepository.save(gym);
			return gymMapper.toDto(saved);
		} catch (BrandException e) {
			throw new GymException(GymException.GYM_NOT_FOUND, "Gym not found");
		} catch (GymException e) {
			throw e;
		} catch (Exception e) {
			logger.error("Unhandled error", e);
			throw new GymException(GymException.CREATION_FAILED, e);
		}
	}

	@Override
	public GymDto update(String brandUuid, GymDto gymDto) throws GymException {
		Gym gym = gymMapper.toEntity(gymDto);
		Gym oldGym = this.readByGymUuid(brandUuid, gym.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration()
			.setSkipNullEnabled(false)
			.setCollectionsMergeEnabled(false);
		
		PropertyMap<Gym, Gym> gymPropertyMap = new PropertyMap<Gym, Gym>() {
			protected void configure() {
				skip().setId(null);
				skip().setActive(false);
			}
		};
		
		mapper.addMappings(gymPropertyMap);
		mapper = gymMapper.initGymMappings(mapper);
		
		mapper.map(gym, oldGym);

		oldGym.setModifiedOn(Instant.now());
		oldGym.setModifiedBy(requestContext.getUsername());

		Gym saved = gymRepository.save(oldGym);
		return gymMapper.toDto(saved);
	}

	@Override
	public GymDto patch(String brandUuid, GymDto gymDto) throws GymException {
		Gym gym = gymMapper.toEntity(gymDto);
		Gym oldGym = this.readByGymUuid(brandUuid, gym.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);

		PropertyMap<Gym, Gym> gymPropertyMap = new PropertyMap<Gym, Gym>() {
			protected void configure() {
				skip().setId(null);
				skip().setContacts(null);
				skip().setPhoneNumbers(null);
			}
		};
		
		mapper.addMappings(gymPropertyMap);
		mapper = gymMapper.initGymMappings(mapper);
		mapper.map(gym, oldGym);

		oldGym.setModifiedOn(Instant.now());
		oldGym.setModifiedBy(requestContext.getUsername());

		Gym saved = gymRepository.save(oldGym);
		return gymMapper.toDto(saved);
	}

	@Override
	public void delete(String brandUuid, String gymUuid) throws GymException {
		Gym entity = this.readByGymUuid(brandUuid, gymUuid);
		entity.setDeleted(true);

		entity.setDeletedOn(Instant.now());
		entity.setDeletedBy(requestContext.getUsername());

		gymRepository.save(entity);
	}

	@Override
	public GymDto activate(String brandUuid, String gymUuid) throws GymException {
		Optional<Gym> entity = gymRepository.activate(brandUuid, gymUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.GYM_NOT_FOUND, "Gym not found");
		}

		return gymMapper.toDto(entity.get());
	}

	@Override
	public GymDto deactivate(String brandUuid, String gymUuid) throws GymException {
		Optional<Gym> entity = gymRepository.deactivate(brandUuid, gymUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.GYM_NOT_FOUND, "Gym not found");
		}

		return gymMapper.toDto(entity.get());
	}
	
	private Gym readByGymUuid(String brandUuid, String gymUuid) throws GymException {
		Optional<Gym> entity = gymRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.GYM_NOT_FOUND, "Gym not found");
		}

		return entity.get();
	}
}