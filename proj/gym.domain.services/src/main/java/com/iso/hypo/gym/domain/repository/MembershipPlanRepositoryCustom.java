package com.iso.hypo.gym.domain.repository;

import java.util.Optional;

import com.iso.hypo.gym.domain.model.aggregate.MembershipPlan;

public interface MembershipPlanRepositoryCustom {

	Optional<MembershipPlan> activate(String brandId, String id);
	
	Optional<MembershipPlan> deactivate(String brandId, String id);
}
