package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.brand.domain.model.Gym;

public interface GymRepository extends PagingAndSortingRepository<Gym, String>, CrudRepository<Gym, String>, GymRepositoryCustom {
	
	Optional<Gym> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String gymUuid);
	
	Optional<Gym> findByBrandUuidAndCode(String brandUuid, String code);
	
	Page<Gym> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<Gym> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);
}

