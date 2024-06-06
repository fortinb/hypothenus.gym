package com.isoceles.hypothenus.gym.domain.repository;

import org.springframework.stereotype.Repository;

import com.isoceles.hypothenus.gym.domain.model.Gym;

@Repository
public class GymRepository {

	public GymRepository() {
		
	}
	
	public Gym save(Gym gym) {
		return gym;
		
	}

}
