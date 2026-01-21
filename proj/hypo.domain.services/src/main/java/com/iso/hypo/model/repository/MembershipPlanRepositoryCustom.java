package com.iso.hypo.model.repository;

import java.util.Optional;

import com.iso.hypo.model.aggregate.MembershipPlan;

public interface MembershipPlanRepositoryCustom {

	Optional<MembershipPlan> activate(String brandUuid, String membershipPlanUuid);
	
	Optional<MembershipPlan> deactivate(String brandUuid, String membershipPlanUuid);
}
