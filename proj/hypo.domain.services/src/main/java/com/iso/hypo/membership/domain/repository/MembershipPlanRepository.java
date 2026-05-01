package com.iso.hypo.membership.domain.repository;

import java.util.Date;
import java.util.Optional;

import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;
import com.iso.hypo.membership.domain.model.MembershipPlan;

public interface MembershipPlanRepository {
	
	Optional<MembershipPlan> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String membershipPlanUuid);
	
	PageResult<MembershipPlan> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest);
	
	PageResult<MembershipPlan> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest);
	
	MembershipPlan save(MembershipPlan membershipPlan);

    void delete(MembershipPlan membershipPlan);

    void deleteAll();
    
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);

	long removeGymReferences(String brandUuid, String gymUuid);

	long removeCourseReferences(String brandUuid, String courseUuid);
	
	PageResult<MembershipPlan> findActiveOnDate(String brandUuid, Date currentDate, PageRequest pageRequest);
}