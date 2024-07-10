package com.isoceles.hypothenus.gym.domain.services;

import java.time.Instant;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.isoceles.hypothenus.gym.domain.context.RequestContext;
import com.isoceles.hypothenus.gym.domain.exception.DomainException;
import com.isoceles.hypothenus.gym.domain.model.GymSearchResult;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;
import com.isoceles.hypothenus.gym.domain.repository.GymRepository;

@Service
public class GymService {

	private GymRepository gymRepository;

	@Autowired
	private RequestContext requestContext;
	
	public GymService(GymRepository gymRepository) {
		this.gymRepository = gymRepository;
	}

	public Gym create(Gym gym) throws DomainException {
		Optional<Gym> existingGym = gymRepository.findByGymId(gym.getGymId());
		if (existingGym.isPresent()) {
			throw new DomainException(DomainException.GYM_CODE_ALREADY_EXIST, "Duplicate gym code");
		}
		
		gym.setCreatedOn(Instant.now());
		gym.setCreatedBy(requestContext.getUsername());
		
		return gymRepository.save(gym);
	}

	public Gym update(Gym gym) throws DomainException {
		Gym oldGym = this.findByGymId(gym.getGymId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		PropertyMap<Gym, Gym> gymPropertyMap = new PropertyMap<Gym, Gym>()
	    {
	        protected void configure()
	        {
	            skip().setId(null);
	        }
	    };
	    
		mapper.addMappings(gymPropertyMap);
		mapper.map(gym, oldGym);

		oldGym.setModifiedOn(Instant.now());
		oldGym.setModifiedBy(requestContext.getUsername());
		
		return gymRepository.save(oldGym);
	}

	public Gym patch(Gym gym) throws DomainException {
		Gym oldGym = this.findByGymId(gym.getGymId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		PropertyMap<Gym, Gym> gymPropertyMap = new PropertyMap<Gym, Gym>()
	    {
	        protected void configure()
	        {
	            skip().setId(null);
	        }
	    };
	    
		mapper.addMappings(gymPropertyMap);
		mapper.map(gym, oldGym);
		
		oldGym.setModifiedOn(Instant.now());
		oldGym.setModifiedBy(requestContext.getUsername());
		
		return gymRepository.save(oldGym);
	}

	public void delete(String gymId) throws DomainException {
		Gym oldGym = this.findByGymId(gymId);
		oldGym.setDeleted(true);

		oldGym.setDeletedOn(Instant.now());
		oldGym.setDeletedBy(requestContext.getUsername());
		
		gymRepository.save(oldGym);
	}

	public Gym findByGymId(String id) throws DomainException {
		Optional<Gym> entity = gymRepository.findByGymIdAndIsDeletedIsFalse(id);
		if (entity.isEmpty()) {
			throw new DomainException(DomainException.GYM_NOT_FOUND, "Gym not found");
		}

		return entity.get();
	}

	public Page<GymSearchResult> search(int page, int pageSize, String criteria, boolean includeInactive) throws DomainException {
		return gymRepository.searchAutocomplete(criteria, PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"),includeInactive);
	}

	public Page<Gym> list(int page, int pageSize, boolean includeInactive) throws DomainException {
		if (includeInactive) {
			return gymRepository.findAllByIsDeletedIsFalse(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
		}
		
		return gymRepository.findAllByIsDeletedIsFalseAndIsActiveIsTrue(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
	}
	
	public Gym activate(String gymId) throws DomainException {
		
		Optional<Gym> oldGym = gymRepository.activate(gymId);
		if (oldGym.isEmpty()) {
			throw new DomainException(DomainException.GYM_NOT_FOUND, "Gym not found");
		}
		
		return oldGym.get();
	}
	
	public Gym deactivate(String gymId) throws DomainException {
		
		Optional<Gym> oldGym = gymRepository.deactivate(gymId);
		if (oldGym.isEmpty()) {
			throw new DomainException(DomainException.GYM_NOT_FOUND, "Gym not found");
		}
		
		return oldGym.get();
	}
}
