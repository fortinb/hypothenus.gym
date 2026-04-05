package com.iso.hypo.repositories;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.iso.hypo.domain.aggregate.MembershipPlan;

public interface MembershipPlanRepositoryCustom {

	Optional<MembershipPlan> activate(String brandUuid, String membershipPlanUuid);
	
	Optional<MembershipPlan> deactivate(String brandUuid, String membershipPlanUuid);
	
	void delete(String brandUuid, String membershipPlanUuid, String deletedBy);
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);

	long removeGymReferences(String gymId);

	long removeCourseReferences(String courseId);
	
	Page<MembershipPlan> findActiveOnDate(String brandUuid, Date currentDate, Pageable pageable);
}

