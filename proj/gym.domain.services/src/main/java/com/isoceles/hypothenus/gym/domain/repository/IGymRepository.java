package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;

public interface IGymRepository extends PagingAndSortingRepository<Gym, String>, CrudRepository<Gym, String>, IGymQueries {
	
	Optional<Gym> findByGymIdAndIsDeletedIsFalse(String gymId);
	
	Page<Gym> findAllByIsDeletedIsFalse(Pageable pageable);
}
