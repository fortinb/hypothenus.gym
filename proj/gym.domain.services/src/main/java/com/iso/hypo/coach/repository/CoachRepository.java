package com.iso.hypo.coach.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.coach.domain.aggregate.Coach;

public interface CoachRepository extends PagingAndSortingRepository<Coach, String>, CrudRepository<Coach, String>, CoachRepositoryCustom {
	
	Optional<Coach> findByBrandIdAndGymIdAndIdAndIsDeletedIsFalse(String brandId, String gymId, String id);
	
	Page<Coach> findAllByBrandIdAndGymIdAndIsDeletedIsFalse(String brandId,String gymId, Pageable pageable);
	
	Page<Coach> findAllByBrandIdAndGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(String brandId,String gymId, Pageable pageable);
	
	
}