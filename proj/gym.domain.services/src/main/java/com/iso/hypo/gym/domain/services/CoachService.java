package com.iso.hypo.gym.domain.services;

import java.time.Instant;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.gym.domain.context.RequestContext;
import com.iso.hypo.gym.domain.exception.DomainException;
import com.iso.hypo.gym.domain.model.aggregate.Coach;
import com.iso.hypo.gym.domain.model.contact.Contact;
import com.iso.hypo.gym.domain.model.contact.Person;
import com.iso.hypo.gym.domain.model.contact.PhoneNumber;
import com.iso.hypo.gym.domain.model.location.Address;
import com.iso.hypo.gym.domain.repository.CoachRepository;

@Service
public class CoachService {

	private CoachRepository coachRepository;

	@Autowired
	private RequestContext requestContext;
	
	public CoachService(CoachRepository coachRepository) {
		this.coachRepository = coachRepository;
	}

	public Coach create(String brandId, String gymId, Coach coach) throws DomainException {
		if (!coach.getBrandId().equals(brandId)) {
			throw new DomainException(DomainException.INVALID_BRAND, "Invalid brand");
		}
		
		if (!coach.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		coach.setCreatedOn(Instant.now());
		coach.setCreatedBy(requestContext.getUsername());
		
		return coachRepository.save(coach);
	}

	public Coach update(String brandId, String gymId, Coach coach) throws DomainException {
		if (!coach.getBrandId().equals(brandId)) {
			throw new DomainException(DomainException.INVALID_BRAND, "Invalid brand");
		}
		
		if (!coach.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		Coach oldCoach = this.findByCoachId(brandId, gymId, coach.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initCoachMappings(mapper);
		mapper.map(coach, oldCoach);
	
		oldCoach.setModifiedOn(Instant.now());
		oldCoach.setModifiedBy(requestContext.getUsername());
		
		return coachRepository.save(oldCoach);
	}

	public Coach patch(String brandId, String gymId, Coach coach) throws DomainException {
		if (!coach.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_BRAND, "Invalid gym");
		}
		
		Coach oldCoach = this.findByCoachId(brandId, gymId, coach.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initCoachMappings(mapper);
		mapper.map(coach, oldCoach);
		
		oldCoach.setModifiedOn(Instant.now());
		oldCoach.setModifiedBy(requestContext.getUsername());
		
		return coachRepository.save(oldCoach);
	}

	public void delete(String brandId, String gymId, String coachId) throws DomainException {
		Coach oldCoach = this.findByCoachId(brandId, gymId,  coachId);
		oldCoach.setDeleted(true);

		oldCoach.setDeletedOn(Instant.now());
		oldCoach.setDeletedBy(requestContext.getUsername());
		
		coachRepository.save(oldCoach);
	}

	public Coach findByCoachId(String brandId, String gymId, String id) throws DomainException {
		Optional<Coach> entity = coachRepository.findByBrandIdAndGymIdAndIdAndIsDeletedIsFalse(brandId, gymId, id);
		if (entity.isEmpty()) {
			throw new DomainException(DomainException.COACH_NOT_FOUND, "Coach not found");
		}

		return entity.get();
	}

	public Page<Coach> list(String brandId, String gymId, int page, int pageSize, boolean includeInactive) throws DomainException {
		if (includeInactive) {
			return coachRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalse(brandId, gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname"));
		}

		return coachRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "person.lastname"));
	}
	
	public Coach activate(String brandId, String gymId, String id) throws DomainException {
		
		Optional<Coach> oldCoach = coachRepository.activate(brandId, gymId, id);
		if (oldCoach.isEmpty()) {
			throw new DomainException(DomainException.COACH_NOT_FOUND, "Coach not found");
		}
		
		return oldCoach.get();
	}
	
	public Coach deactivate(String brandId, String gymId, String id) throws DomainException {
		
		Optional<Coach> oldCoach = coachRepository.deactivate(brandId, gymId, id);
		if (oldCoach.isEmpty()) {
			throw new DomainException(DomainException.COACH_NOT_FOUND, "Coach not found");
		}
		
		return oldCoach.get();
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
