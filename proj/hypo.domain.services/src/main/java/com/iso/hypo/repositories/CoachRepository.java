package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.domain.aggregate.Coach;

public interface CoachRepository extends PagingAndSortingRepository<Coach, String>, CrudRepository<Coach, String>, CoachRepositoryCustom {
	
	Optional<Coach> findByBrandUuidAndUuidAndIsDeletedIsFalse(String brandUuid, String coachUuid);
	
	Page<Coach> findAllByBrandUuidAndIsDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<Coach> findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(String brandUuid, Pageable pageable);
}