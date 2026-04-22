package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.brand.domain.model.Coach;

public interface CoachRepository extends PagingAndSortingRepository<Coach, String>, CrudRepository<Coach, String>, CoachRepositoryCustom {
	
	Optional<Coach> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String coachUuid);
	
	Page<Coach> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<Coach> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);
}