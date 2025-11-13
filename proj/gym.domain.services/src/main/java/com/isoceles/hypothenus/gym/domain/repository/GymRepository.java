package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Gym;

public interface GymRepository extends PagingAndSortingRepository<Gym, String>, CrudRepository<Gym, String>, GymRepositoryCustom {
	
	Optional<Gym> findByBrandIdAndGymIdAndIsDeletedIsFalse(String brandId, String gymId);
	
	Optional<Gym> findByBrandIdAndGymId(String brandId, String gymId);
	
	Page<Gym> findAllByBrandIdAndIsDeletedIsFalse(String brandId, Pageable pageable);
	
	Page<Gym> findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(String brandId, Pageable pageable);
}
