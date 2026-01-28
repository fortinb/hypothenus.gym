package com.iso.hypo.repositories;

import java.util.Optional;

import com.iso.hypo.domain.aggregate.MembershipPlan;

public interface MembershipPlanRepositoryCustom {

	Optional<MembershipPlan> activate(String brandUuid, String membershipPlanUuid);
	
	Optional<MembershipPlan> deactivate(String brandUuid, String membershipPlanUuid);
	
	void delete(String brandUuid, String membershipPlanUuid, String deletedBy);
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}

