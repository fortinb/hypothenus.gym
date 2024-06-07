package com.isoceles.hypothenus.gym.domain.services;

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
}
