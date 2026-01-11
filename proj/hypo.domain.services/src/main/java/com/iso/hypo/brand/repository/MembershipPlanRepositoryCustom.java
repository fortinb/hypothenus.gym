package com.iso.hypo.brand.repository;

import java.util.Optional;

import com.iso.hypo.brand.domain.aggregate.MembershipPlan;

public interface MembershipPlanRepositoryCustom {

	Optional<MembershipPlan> activate(String brandId, String id);
	
	Optional<MembershipPlan> deactivate(String brandId, String id);
}
