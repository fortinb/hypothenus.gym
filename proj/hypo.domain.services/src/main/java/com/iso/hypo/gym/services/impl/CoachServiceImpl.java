package com.iso.hypo.gym.services.impl;

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
import com.iso.hypo.gym.exception.GymException;
import com.iso.hypo.gym.domain.aggregate.Coach;
import com.iso.hypo.common.domain.contact.Contact;
import com.iso.hypo.common.domain.contact.Person;
import com.iso.hypo.common.domain.contact.PhoneNumber;
import com.iso.hypo.common.domain.location.Address;
import com.iso.hypo.gym.repository.CoachRepository;
import com.iso.hypo.gym.dto.CoachDto;
import com.iso.hypo.gym.mappers.GymMapper;
import com.iso.hypo.gym.services.CoachService;

@Service
public class CoachServiceImpl implements CoachService {

	private CoachRepository coachRepository;

	private GymMapper gymMapper;

	@Autowired
	private RequestContext requestContext;

	public CoachServiceImpl(CoachRepository coachRepository, GymMapper gymMapper) {
		this.coachRepository = coachRepository;
		this.gymMapper = gymMapper;
	}

	@Override
	public CoachDto create(String brandId, String gymId, CoachDto coachDto) throws GymException {
		Coach coach = gymMapper.toEntity(coachDto);
		if (!coach.getBrandId().equals(brandId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid brand");
		}

		if (!coach.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_GYM, "Invalid gym");
		}

		coach.setUuid(UUID.randomUUID().toString());
		coach.setCreatedOn(Instant.now());
		coach.setCreatedBy(requestContext.getUsername());

		Coach saved = coachRepository.save(coach);
		return gymMapper.toDto(saved);
	}

	@Override
	public CoachDto update(String brandId, String gymId, CoachDto coachDto) throws GymException {
		Coach coach = gymMapper.toEntity(coachDto);
		if (!coach.getBrandId().equals(brandId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid brand");
		}

		if (!coach.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_GYM, "Invalid gym");
		}

		Coach oldCoach = this.readByCoachUuid(brandId, gymId, coach.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initCoachMappings(mapper);
		mapper.map(coach, oldCoach);
	
		oldCoach.setModifiedOn(Instant.now());
		oldCoach.setModifiedBy(requestContext.getUsername());
		
		Coach saved = coachRepository.save(oldCoach);
		return gymMapper.toDto(saved);
	}

	@Override
	public CoachDto patch(String brandId, String gymId, CoachDto coachDto) throws GymException {
		Coach coach = gymMapper.toEntity(coachDto);
		if (!coach.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid gym");
		}
		
		Coach oldCoach = this.readByCoachUuid(brandId, gymId, coach.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initCoachMappings(mapper);
		mapper.map(coach, oldCoach);
		
		oldCoach.setModifiedOn(Instant.now());
		oldCoach.setModifiedBy(requestContext.getUsername());
		
		Coach saved = coachRepository.save(oldCoach);
		return gymMapper.toDto(saved);
	}

	@Override
	public void delete(String brandId, String gymId, String coachUuid) throws GymException {
		Coach entity = this.readByCoachUuid(brandId, gymId, coachUuid);
		entity.setDeleted(true);

		entity.setDeletedOn(Instant.now());
		entity.setDeletedBy(requestContext.getUsername());
		
		coachRepository.save(entity);
	}

	@Override
	public CoachDto findByCoachUuid(String brandId, String gymId, String coachUuid) throws GymException {
		Optional<Coach> entity = coachRepository.findByBrandIdAndGymIdAndUuidAndIsDeletedIsFalse(brandId, gymId, coachUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.COACH_NOT_FOUND, "Coach not found");
		}

		return gymMapper.toDto(entity.get());
	}

	@Override
	public Page<CoachDto> list(String brandId, String gymId, int page, int pageSize, boolean includeInactive) throws GymException {
		if (includeInactive) {
			return coachRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalse(brandId, gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname")).map(c -> gymMapper.toDto(c));
		}

		return coachRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname")).map(c -> gymMapper.toDto(c));
	}

	@Override
	public CoachDto activate(String brandId, String gymId, String coachUuid) throws GymException {
		Optional<Coach> entity = coachRepository.activate(brandId, gymId, coachUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.COACH_NOT_FOUND, "Coach not found");
		}
		
		return gymMapper.toDto(entity.get());
	}
	
	@Override
	public CoachDto deactivate(String brandId, String gymId, String coachUuid) throws GymException {
		Optional<Coach> entity = coachRepository.deactivate(brandId, gymId, coachUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.COACH_NOT_FOUND, "Coach not found");
		}
		
		return gymMapper.toDto(entity.get());
	}
	
	private Coach readByCoachUuid(String brandId, String gymId, String coachUuid) throws GymException {
		Optional<Coach> entity = coachRepository.findByBrandIdAndGymIdAndUuidAndIsDeletedIsFalse(brandId, gymId, coachUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.COACH_NOT_FOUND, "Coach not found");
		}

		return entity.get();
	}
	
	private ModelMapper initCoachMappings(ModelMapper mapper) {
		PropertyMap<Coach, Coach> coachPropertyMap = new PropertyMap<Coach, Coach>()
	    {
	        protected void configure()
	        {
	            skip().setId(null);
	            skip().setActive(false);
	            skip().setActivatedOn(null);
	            skip().setDeactivatedOn(null);
	        }
	    };
		
	    PropertyMap<Person, Person> personPropertyMap = new PropertyMap<Person, Person>() {
			@Override
			protected void configure() {

			}
		};
		
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
		mapper.addMappings(coachPropertyMap);
		mapper.addMappings(personPropertyMap);
		mapper.addMappings(addressPropertyMap);
		mapper.addMappings(phoneNumberPropertyMap);
		mapper.addMappings(contactPropertyMap);
		
		return mapper;
	}
}
