package com.isoceles.hypothenus.gym.domain.services;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.stereotype.Service;

import com.isoceles.hypothenus.gym.domain.exception.DomainException;
import com.isoceles.hypothenus.gym.domain.model.Gym;
import com.isoceles.hypothenus.gym.domain.repository.IGymRepository;

@Service
public class GymService {

	private IGymRepository gymRepository;
	
	public GymService(IGymRepository gymRepository) {
		this.gymRepository = gymRepository;
	}

	public Gym create(Gym gym) throws DomainException {
		return gymRepository.save(gym);
	}

	public Gym findById(String id) throws DomainException {
		Optional<Gym> entity = gymRepository.findById(id);
		if (entity.isEmpty()) {
			throw new DomainException(DomainException.GYM_NOT_FOUND, "Gym not found");
		}
		
		return entity.get();
			
	}
	
	public Page<Gym> search(String criteria, int page, int pageSize) throws DomainException {
		return gymRepository.findAllBy(TextCriteria.forDefaultLanguage().matching(criteria), PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
	}
	
	public Page<Gym> list(int page, int pageSize) throws DomainException {
		return gymRepository.findAll(PageRequest.of(page, pageSize, Sort.Direction.ASC, "name"));
	}
}
