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
import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.contact.Contact;
import com.iso.hypo.domain.contact.Person;
import com.iso.hypo.domain.contact.PhoneNumber;
import com.iso.hypo.domain.location.Address;
import com.iso.hypo.domain.dto.CoachDto;
import com.iso.hypo.services.exception.CoachException;
import com.iso.hypo.services.mappers.GymMapper;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.repositories.GymRepository;
import com.iso.hypo.services.CoachService;

@Service
public class CoachServiceImpl implements CoachService {

	private GymRepository gymRepository;
	
	private CoachRepository coachRepository;

	private GymMapper gymMapper;

	@Autowired
	private RequestContext requestContext;

	public CoachServiceImpl(GymRepository gymRepository, CoachRepository coachRepository, GymMapper gymMapper) {
		this.gymRepository = gymRepository;
		this.coachRepository = coachRepository;
		this.gymMapper = gymMapper;
	}

	@Override
	public CoachDto create(String brandUuid, String gymUuid, CoachDto coachDto) throws CoachException {
		Optional<Gym> existingGym = gymRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid);
		if (!existingGym.isPresent()) {
			throw new CoachException(CoachException.GYM_NOT_FOUND, "Gym not found");
		}
		
		Coach coach = gymMapper.toEntity(coachDto);
		
		if (!coach.getBrandUuid().equals(brandUuid)) {
			throw new CoachException(CoachException.INVALID_BRAND, "Invalid brand");
		}

		if (!coach.getGymUuid().equals(gymUuid)) {
			throw new CoachException(CoachException.INVALID_GYM, "Invalid gym");
		}

		coach.setUuid(UUID.randomUUID().toString());
		coach.setCreatedOn(Instant.now());
		coach.setCreatedBy(requestContext.getUsername());

		Coach saved = coachRepository.save(coach);
		return gymMapper.toDto(saved);
	}

	@Override
	public CoachDto update(String brandUuid, String gymUuid, CoachDto coachDto) throws CoachException {
		Coach coach = gymMapper.toEntity(coachDto);
		if (!coach.getBrandUuid().equals(brandUuid)) {
			throw new CoachException(CoachException.INVALID_BRAND, "Invalid brand");
		}

		if (!coach.getGymUuid().equals(gymUuid)) {
			throw new CoachException(CoachException.INVALID_GYM, "Invalid gym");
		}

		Coach oldCoach = this.readByCoachUuid(brandUuid, gymUuid, coach.getUuid());

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
	public CoachDto patch(String brandUuid, String gymUuid, CoachDto coachDto) throws CoachException {
		Coach coach = gymMapper.toEntity(coachDto);
		if (!coach.getGymUuid().equals(gymUuid)) {
			throw new CoachException(CoachException.INVALID_BRAND, "Invalid gym");
		}
		
		Coach oldCoach = this.readByCoachUuid(brandUuid, gymUuid, coach.getUuid());

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
	public void delete(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		Coach entity = this.readByCoachUuid(brandUuid, gymUuid, coachUuid);
		entity.setDeleted(true);

		entity.setDeletedOn(Instant.now());
		entity.setDeletedBy(requestContext.getUsername());
		
		coachRepository.save(entity);
	}

	@Override
	public CoachDto findByCoachUuid(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		Optional<Coach> entity = coachRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid, coachUuid);
		if (entity.isEmpty()) {
			throw new CoachException(CoachException.COACH_NOT_FOUND, "Coach not found");
		}

		return gymMapper.toDto(entity.get());
	}

	@Override
	public Page<CoachDto> list(String brandUuid, String gymUuid, int page, int pageSize, boolean includeInactive) throws CoachException {
		if (includeInactive) {
			return coachRepository.findAllByBrandUuidAndGymUuidAndIsDeletedIsFalse(brandUuid, gymUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname")).map(c -> gymMapper.toDto(c));
		}

		return coachRepository.findAllByBrandUuidAndGymUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, gymUuid, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname")).map(c -> gymMapper.toDto(c));
	}

	@Override
	public CoachDto activate(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		Optional<Coach> entity = coachRepository.activate(brandUuid, gymUuid, coachUuid);
		if (entity.isEmpty()) {
			throw new CoachException(CoachException.COACH_NOT_FOUND, "Coach not found");
		}
		
		return gymMapper.toDto(entity.get());
	}
	
	@Override
	public CoachDto deactivate(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		Optional<Coach> entity = coachRepository.deactivate(brandUuid, gymUuid, coachUuid);
		if (entity.isEmpty()) {
			throw new CoachException(CoachException.COACH_NOT_FOUND, "Coach not found");
		}
		
		return gymMapper.toDto(entity.get());
	}
	
	private Coach readByCoachUuid(String brandUuid, String gymUuid, String coachUuid) throws CoachException {
		Optional<Coach> entity = coachRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid, coachUuid);
		if (entity.isEmpty()) {
			throw new CoachException(CoachException.COACH_NOT_FOUND, "Coach not found");
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


