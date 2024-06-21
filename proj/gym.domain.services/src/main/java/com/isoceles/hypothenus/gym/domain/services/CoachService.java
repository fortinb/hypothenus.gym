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
import com.isoceles.hypothenus.gym.domain.model.aggregate.Coach;
import com.isoceles.hypothenus.gym.domain.repository.CoachRepository;

@Service
public class CoachService {

	private CoachRepository coachRepository;

	@Autowired
	private RequestContext requestContext;
	
	public CoachService(CoachRepository coachRepository) {
		this.coachRepository = coachRepository;
	}

	public Coach create(String gymId, Coach coach) throws DomainException {
		if (!coach.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		coach.setCreatedOn(Instant.now());
		coach.setCreatedBy(requestContext.getUsername());
		
		return coachRepository.save(coach);
	}

	public Coach update(String gymId, Coach coach) throws DomainException {
		if (!coach.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		Coach oldCoach = this.findByCoachId(gymId, coach.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
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
	    
		mapper.addMappings(coachPropertyMap);
		mapper.map(coach, oldCoach);

		oldCoach.setModifiedOn(Instant.now());
		oldCoach.setModifiedBy(requestContext.getUsername());
		
		return coachRepository.save(oldCoach);
	}

	public Coach patch(String gymId, Coach coach) throws DomainException {
		if (!coach.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		Coach oldCoach = this.findByCoachId(gymId, coach.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
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
	    
		mapper.addMappings(coachPropertyMap);
		mapper.map(coach, oldCoach);
		
		oldCoach.setModifiedOn(Instant.now());
		oldCoach.setModifiedBy(requestContext.getUsername());
		
		return coachRepository.save(oldCoach);
	}

	public void delete(String gymId, String coachId) throws DomainException {
		Coach oldCoach = this.findByCoachId(gymId,  coachId);
		oldCoach.setDeleted(true);

		oldCoach.setDeletedOn(Instant.now());
		oldCoach.setDeletedBy(requestContext.getUsername());
		
		coachRepository.save(oldCoach);
	}

	public Coach findByCoachId(String gymId, String id) throws DomainException {
		Optional<Coach> entity = coachRepository.findByGymIdAndIdAndIsDeletedIsFalse(gymId, id);
		if (entity.isEmpty()) {
			throw new DomainException(DomainException.COACH_NOT_FOUND, "Coach not found");
		}

		return entity.get();
	}

	public Page<Coach> list(String gymId, int page, int pageSize) throws DomainException {
		return coachRepository.findAllByGymIdAndIsDeletedIsFalse(gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
	}
	
	public Page<Coach> listActive(String gymId, int page, int pageSize) throws DomainException {
		return coachRepository.findAllByGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
	}
	
	public Coach activate(String gymId, String id) throws DomainException {
		
		Optional<Coach> oldCoach = coachRepository.activate(gymId, id);
		if (oldCoach.isEmpty()) {
			throw new DomainException(DomainException.COACH_NOT_FOUND, "Coach not found");
		}
		
		return oldCoach.get();
	}
	
	public Coach deactivate(String gymId, String id) throws DomainException {
		
		Optional<Coach> oldCoach = coachRepository.deactivate(gymId, id);
		if (oldCoach.isEmpty()) {
			throw new DomainException(DomainException.COACH_NOT_FOUND, "Coach not found");
		}
		
		return oldCoach.get();
	}
}
