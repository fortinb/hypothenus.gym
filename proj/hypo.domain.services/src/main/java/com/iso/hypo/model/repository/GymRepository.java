package com.iso.hypo.model.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.model.aggregate.Gym;

public interface GymRepository extends PagingAndSortingRepository<Gym, String>, CrudRepository<Gym, String>, GymRepositoryCustom {
	
	Optional<Gym> findByBrandUuidAndUuidAndIsDeletedIsFalse(String brandUuid, String gymUuid);
	
	Optional<Gym> findByBrandUuidAndCode(String brandUuid, String code);
	
	Page<Gym> findAllByBrandUuidAndIsDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<Gym> findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(String brandUuid, Pageable pageable);
}
