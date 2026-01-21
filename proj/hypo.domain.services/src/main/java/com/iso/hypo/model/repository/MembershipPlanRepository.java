package com.iso.hypo.model.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.model.aggregate.MembershipPlan;

public interface MembershipPlanRepository extends PagingAndSortingRepository<MembershipPlan, String>, CrudRepository<MembershipPlan, String>, MembershipPlanRepositoryCustom {
	
	Optional<MembershipPlan> findByBrandUuidAndUuidAndIsDeletedIsFalse(String brandUuid, String membershipPlanUuid);
	
	Page<MembershipPlan> findAllByBrandUuidAndIsDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<MembershipPlan> findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(String brandUuid, Pageable pageable);
	
	
}