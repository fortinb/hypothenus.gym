package com.iso.hypo.membershipPlan.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.membershipPlan.domain.aggregate.MembershipPlan;

public interface MembershipPlanRepository extends PagingAndSortingRepository<MembershipPlan, String>, CrudRepository<MembershipPlan, String>, MembershipPlanRepositoryCustom {
	
	Optional<MembershipPlan> findByBrandIdAndIdAndIsDeletedIsFalse(String brandId, String id);
	
	Page<MembershipPlan> findAllByBrandIdAndIsDeletedIsFalse(String brandId, Pageable pageable);
	
	Page<MembershipPlan> findAllByBrandIdAndIsDeletedIsFalseAndIsActiveIsTrue(String brandId, Pageable pageable);
	
	
}