package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.contact.Contact;
import com.iso.hypo.domain.contact.PhoneNumber;
import com.iso.hypo.domain.location.Address;
import com.iso.hypo.domain.dto.GymDto;
import com.iso.hypo.domain.dto.GymSearchDto;
import com.iso.hypo.services.exception.GymException;
import com.iso.hypo.services.mappers.GymMapper;
import com.iso.hypo.repositories.BrandRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.services.GymService;

@Service
public class GymServiceImpl implements GymService {

	private BrandRepository brandRepository;
	
	private GymRepository gymRepository;

	private GymMapper gymMapper;

	@Autowired
	private RequestContext requestContext;

	public GymServiceImpl(BrandRepository brandRepository, GymRepository gymRepository, GymMapper gymMapper) {
		this.brandRepository = brandRepository;
		this.gymRepository = gymRepository;
		this.gymMapper = gymMapper;
	}

	@Override
	public GymDto create(GymDto gymDto) throws GymException {
		Optional<Brand> existingBrand = brandRepository.findByUuidAndIsDeletedIsFalse(gymDto.getBrandUuid());
		if (!existingBrand.isPresent()) {
			throw new GymException(GymException.BRAND_NOT_FOUND, "Brand not found");
		}
		
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
		mapper = initGymMappings(mapper);
		
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
		mapper = initGymMappings(mapper);
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
	public GymDto findByCode(String brandUuid, String gymUuid) throws GymException {
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
	
	private ModelMapper initGymMappings(ModelMapper mapper) {
		
		PropertyMap<Address, Address> addressPropertyMap = new PropertyMap<Address, Address>() {
			@Override
			protected void configure() {
			}
		};
		
		PropertyMap<PhoneNumber, PhoneNumber> phoneNumberPropertyMap = new PropertyMap<PhoneNumber, PhoneNumber>() {
			@Override
			protected void configure() {
			}
		};
			
		PropertyMap<Contact, Contact> contactPropertyMap = new PropertyMap<Contact, Contact>() {
			@Override
			protected void configure() {
			}
		};

		mapper.addMappings(addressPropertyMap);
		mapper.addMappings(phoneNumberPropertyMap);
		mapper.addMappings(contactPropertyMap);
		
		return mapper;
	}
}


