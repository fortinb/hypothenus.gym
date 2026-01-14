package com.iso.hypo.gym.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.gym.domain.aggregate.Coach;

public interface CoachRepository extends PagingAndSortingRepository<Coach, String>, CrudRepository<Coach, String>, CoachRepositoryCustom {
	
	Optional<Coach> findByBrandIdAndGymIdAndUuidAndIsDeletedIsFalse(String brandId, String gymId, String coachUuid);
	
	Page<Coach> findAllByBrandIdAndGymIdAndIsDeletedIsFalse(String brandId,String gymId, Pageable pageable);
	
	Page<Coach> findAllByBrandIdAndGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(String brandId,String gymId, Pageable pageable);
	
	
}