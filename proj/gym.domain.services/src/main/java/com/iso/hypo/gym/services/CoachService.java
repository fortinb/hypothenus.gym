package com.iso.hypo.gym.services;

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
import com.iso.hypo.gym.domain.aggregate.Coach;
import com.iso.hypo.common.domain.contact.Contact;
import com.iso.hypo.common.domain.contact.Person;
import com.iso.hypo.common.domain.contact.PhoneNumber;
import com.iso.hypo.common.domain.location.Address;
import com.iso.hypo.gym.repository.CoachRepository;
import com.iso.hypo.gym.dto.CoachDto;
import com.iso.hypo.gym.mappers.GymMapper;

@Service
public class CoachService {

	private CoachRepository coachRepository;

	private GymMapper gymMapper;

	@Autowired
	private RequestContext requestContext;

	public CoachService(CoachRepository coachRepository, GymMapper gymMapper) {
		this.coachRepository = coachRepository;
		this.gymMapper = gymMapper;
	}

	public CoachDto create(String brandId, String gymId, CoachDto coachDto) throws GymException {
		Coach coach = gymMapper.toEntity(coachDto);
		if (!coach.getBrandId().equals(brandId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid brand");
		}

		if (!coach.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_GYM, "Invalid gym");
		}

		coach.setCreatedOn(Instant.now());
		coach.setCreatedBy(requestContext.getUsername());

		Coach saved = coachRepository.save(coach);
		return gymMapper.toDto(saved);
	}

	public CoachDto update(String brandId, String gymId, CoachDto coachDto) throws GymException {
		Coach coach = gymMapper.toEntity(coachDto);
		if (!coach.getBrandId().equals(brandId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid brand");
		}

		if (!coach.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_GYM, "Invalid gym");
		}

		Coach oldCoach = this.gymMapper.toEntity(this.findByCoachId(brandId, gymId, coach.getId()));

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initCoachMappings(mapper);
		mapper.map(coach, oldCoach);
	
		oldCoach.setModifiedOn(Instant.now());
		oldCoach.setModifiedBy(requestContext.getUsername());
		
		Coach saved = coachRepository.save(oldCoach);
		return gymMapper.toDto(saved);
	}

	public CoachDto patch(String brandId, String gymId, CoachDto coachDto) throws GymException {
		Coach coach = gymMapper.toEntity(coachDto);
		if (!coach.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid gym");
		}
		
		Coach oldCoach = this.gymMapper.toEntity(this.findByCoachId(brandId, gymId, coach.getId()));

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initCoachMappings(mapper);
		mapper.map(coach, oldCoach);
		
		oldCoach.setModifiedOn(Instant.now());
		oldCoach.setModifiedBy(requestContext.getUsername());
		
		Coach saved = coachRepository.save(oldCoach);
		return gymMapper.toDto(saved);
	}

	public void delete(String brandId, String gymId, String coachId) throws GymException {
		Coach oldCoach = gymMapper.toEntity(this.findByCoachId(brandId, gymId, coachId));
		oldCoach.setDeleted(true);

		oldCoach.setDeletedOn(Instant.now());
		oldCoach.setDeletedBy(requestContext.getUsername());
		
		coachRepository.save(oldCoach);
	}

	public CoachDto findByCoachId(String brandId, String gymId, String id) throws GymException {
		Optional<Coach> entity = coachRepository.findByBrandIdAndGymIdAndIdAndIsDeletedIsFalse(brandId, gymId, id);
		if (entity.isEmpty()) {
			throw new GymException(GymException.COACH_NOT_FOUND, "Coach not found");
		}

		return gymMapper.toDto(entity.get());
	}

	public Page<CoachDto> list(String brandId, String gymId, int page, int pageSize, boolean includeInactive) throws GymException {
		if (includeInactive) {
			return coachRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalse(brandId, gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname")).map(c -> gymMapper.toDto(c));
		}

		return coachRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname")).map(c -> gymMapper.toDto(c));
	}
	
	public CoachDto activate(String brandId, String gymId, String id) throws GymException {
		
		Optional<Coach> oldCoach = coachRepository.activate(brandId, gymId, id);
		if (oldCoach.isEmpty()) {
			throw new GymException(GymException.COACH_NOT_FOUND, "Coach not found");
		}
		
		return gymMapper.toDto(oldCoach.get());
	}
	
	public CoachDto deactivate(String brandId, String gymId, String id) throws GymException {
		
		Optional<Coach> oldCoach = coachRepository.deactivate(brandId, gymId, id);
		if (oldCoach.isEmpty()) {
			throw new GymException(GymException.COACH_NOT_FOUND, "Coach not found");
		}
		
		return gymMapper.toDto(oldCoach.get());
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