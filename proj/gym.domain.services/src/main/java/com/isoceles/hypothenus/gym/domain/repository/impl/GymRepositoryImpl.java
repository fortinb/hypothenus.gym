package com.isoceles.hypothenus.gym.domain.repository.impl;

import org.springframework.stereotype.Repository;

import com.isoceles.hypothenus.gym.domain.model.Gym;

@Repository
public class GymRepositoryImpl {

	public GymRepositoryImpl() {
		
	}
	
	public Gym save(Gym gym) {
		return gym;
		
	}

}
