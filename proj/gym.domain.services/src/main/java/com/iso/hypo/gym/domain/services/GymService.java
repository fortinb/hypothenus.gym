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
import com.iso.hypo.gym.domain.model.aggregate.Gym;
import com.iso.hypo.gym.domain.model.contact.Contact;
import com.iso.hypo.gym.domain.model.contact.PhoneNumber;
import com.iso.hypo.gym.domain.model.location.Address;
import com.iso.hypo.gym.domain.model.search.GymSearchResult;
import com.iso.hypo.gym.domain.repository.GymRepository;

@Service
public class GymService {

	private GymRepository gymRepository;

	@Autowired
	private RequestContext requestContext;

	public GymService(GymRepository gymRepository) {
		this.gymRepository = gymRepository;
	}

	public Gym create(Gym gym) throws DomainException {
		Optional<Gym> existingGym = gymRepository.findByBrandIdAndGymId(gym.getBrandId(), gym.getGymId());
		if (existingGym.isPresent()) {
			throw new DomainException(DomainException.GYM_CODE_ALREADY_EXIST, "Duplicate gym code");
		}

		gym.setCreatedOn(Instant.now());
		gym.setCreatedBy(requestContext.getUsername());

		return gymRepository.save(gym);
	}

	public Gym update(Gym gym) throws DomainException {
		Gym oldGym = this.findByGymId(gym.getBrandId(), gym.getGymId());

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

		return gymRepository.save(oldGym);
	}

	public Gym patch(Gym gym) throws DomainException {
		Gym oldGym = this.findByGymId(gym.getBrandId(), gym.getGymId());

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

		return gymRepository.save(oldGym);
	}

	public void delete(String brandId, String gymId) throws DomainException {
		Gym oldGym = this.findByGymId(brandId, gymId);
		oldGym.setDeleted(true);

		oldGym.setDeletedOn(Instant.now());
		oldGym.setDeletedBy(requestContext.getUsername());

		gymRepository.save(oldGym);
	}

	public Gym findByGymId(String brandId, String id) throws DomainException {
		Optional<Gym> entity = gymRepository.findByBrandIdAndGymIdAndIsDeletedIsFalse(brandId, id);
		if (entity.isEmpty()) {
			throw new DomainException(DomainException.GYM_NOT_FOUND, "Gym not found");
		}

		return entity.get();
	}

	public Page<GymSearchResult> search(int page, int pageSize, String criteria, boolean includeInactive)
			throws DomainException {
		return gymRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),
				includeInactive);
	}

	public Page<Gym> list(String brandId, int page, int pageSize, boolean includeInactive) throws DomainException {
		if (includeInactive) {
			return gymRepository.findAllByBrandIdAndIsDeletedIsFalse(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
		}

		return gymRepository
				.findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
	}

	public Gym activate(String brandId, String gymId) throws DomainException {

		Optional<Gym> oldGym = gymRepository.activate(brandId, gymId);
		if (oldGym.isEmpty()) {
			throw new DomainException(DomainException.GYM_NOT_FOUND, "Gym not found");
		}

		return oldGym.get();
	}

	public Gym deactivate(String brandId, String gymId) throws DomainException {

		Optional<Gym> oldGym = gymRepository.deactivate(brandId, gymId);
		if (oldGym.isEmpty()) {
			throw new DomainException(DomainException.GYM_NOT_FOUND, "Gym not found");
		}

		return oldGym.get();
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
