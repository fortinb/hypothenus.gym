package com.iso.hypo.membership.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.membership.domain.model.MembershipPlan;

public interface MembershipPlanRepository extends PagingAndSortingRepository<MembershipPlan, String>, CrudRepository<MembershipPlan, String>, MembershipPlanRepositoryCustom {
	
	Optional<MembershipPlan> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String membershipPlanUuid);
	
	Page<MembershipPlan> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<MembershipPlan> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);
}