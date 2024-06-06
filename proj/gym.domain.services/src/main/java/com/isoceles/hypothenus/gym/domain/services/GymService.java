package com.isoceles.hypothenus.gym.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.isoceles.hypothenus.gym.domain.exception.DomainException;
import com.isoceles.hypothenus.gym.domain.model.Gym;
import com.isoceles.hypothenus.gym.domain.repository.GymRepository;

@Service
public class GymService {


	private GymRepository gymRepository;
	
	public GymService(GymRepository gymRepository) {
		this.gymRepository = gymRepository;
	}

	public Gym create(Gym gym) throws DomainException {

		return gymRepository.save(gym);
	}
}
