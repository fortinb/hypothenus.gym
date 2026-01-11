package com.iso.hypo.gym.services.impl;

import java.time.Instant;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.gym.exception.GymException;
import com.iso.hypo.gym.domain.aggregate.Gym;
import com.iso.hypo.common.domain.contact.Contact;
import com.iso.hypo.common.domain.contact.PhoneNumber;
import com.iso.hypo.common.domain.location.Address;
import com.iso.hypo.gym.dto.GymSearchResult;
import com.iso.hypo.gym.dto.GymDto;
import com.iso.hypo.gym.repository.GymRepository;
import com.iso.hypo.gym.mappers.GymMapper;
import com.iso.hypo.gym.services.GymService;

@Service
public class GymServiceImpl implements GymService {

	private GymRepository gymRepository;

	private GymMapper gymMapper;

	@Autowired
	private RequestContext requestContext;

	public GymServiceImpl(GymRepository gymRepository, GymMapper gymMapper) {
		this.gymRepository = gymRepository;
		this.gymMapper = gymMapper;
	}

	@Override
	public GymDto create(GymDto gymDto) throws GymException {
		Gym gym = gymMapper.toEntity(gymDto);
		Optional<Gym> existingGym = gymRepository.findByBrandIdAndGymId(gym.getBrandId(), gym.getGymId());
		if (existingGym.isPresent()) {
			throw new GymException(GymException.GYM_CODE_ALREADY_EXIST, "Duplicate gym code");
		}

		gym.setCreatedOn(Instant.now());
		gym.setCreatedBy(requestContext.getUsername());

		Gym saved = gymRepository.save(gym);
		return gymMapper.toDto(saved);
	}

	@Override
	public GymDto update(String brandId, GymDto gymDto) throws GymException {
		Gym gym = gymMapper.toEntity(gymDto);
		Gym oldGym = this.gymMapper.toEntity(this.findByGymId(gym.getBrandId(), gym.getGymId()));

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
	public GymDto patch(String brandId, GymDto gymDto) throws GymException {
		Gym gym = gymMapper.toEntity(gymDto);
		Gym oldGym = this.gymMapper.toEntity(this.findByGymId(gym.getBrandId(), gym.getGymId()));

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
	public void delete(String brandId, String gymId) throws GymException {
		Gym oldGym = gymMapper.toEntity(this.findByGymId(brandId, gymId));
		oldGym.setDeleted(true);

		oldGym.setDeletedOn(Instant.now());
		oldGym.setDeletedBy(requestContext.getUsername());

		gymRepository.save(oldGym);
	}

	@Override
	public GymDto findByGymId(String brandId, String id) throws GymException {
		Optional<Gym> entity = gymRepository.findByBrandIdAndGymIdAndIsDeletedIsFalse(brandId, id);
		if (entity.isEmpty()) {
			throw new GymException(GymException.GYM_NOT_FOUND, "Gym not found");
		}

		return gymMapper.toDto(entity.get());
	}

	@Override
	public Page<GymSearchResult> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws GymException {
		return gymRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),
				includeInactive);
	}

	@Override
	public Page<GymDto> list(String brandId, int page, int pageSize, boolean includeInactive) throws GymException {
		if (includeInactive) {
			return gymRepository.findAllByBrandIdAndIsDeletedIsFalse(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
				.map(g -> gymMapper.toDto(g));
		}

		return gymRepository
				.findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"))
				.map(g -> gymMapper.toDto(g));
	}

	@Override
	public GymDto activate(String brandId, String gymId) throws GymException {

		Optional<Gym> oldGym = gymRepository.activate(brandId, gymId);
		if (oldGym.isEmpty()) {
			throw new GymException(GymException.GYM_NOT_FOUND, "Gym not found");
		}

		return gymMapper.toDto(oldGym.get());
	}

	@Override
	public GymDto deactivate(String brandId, String gymId) throws GymException {

		Optional<Gym> oldGym = gymRepository.deactivate(brandId, gymId);
		if (oldGym.isEmpty()) {
			throw new GymException(GymException.GYM_NOT_FOUND, "Gym not found");
		}

		return gymMapper.toDto(oldGym.get());
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
