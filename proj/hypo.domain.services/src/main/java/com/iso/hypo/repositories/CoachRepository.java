package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.domain.aggregate.Coach;

public interface CoachRepository extends PagingAndSortingRepository<Coach, String>, CrudRepository<Coach, String>, CoachRepositoryCustom {
	
	Optional<Coach> findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(String brandUuid, String gymUuid, String coachUuid);
	
	Page<Coach> findAllByBrandUuidAndGymUuidAndIsDeletedIsFalse(String brandUuid,String gymUuid, Pageable pageable);
	
	Page<Coach> findAllByBrandUuidAndGymUuidAndIsDeletedIsFalseAndIsActiveIsTrue(String brandUuid,String gymUuid, Pageable pageable);
	
	
}

