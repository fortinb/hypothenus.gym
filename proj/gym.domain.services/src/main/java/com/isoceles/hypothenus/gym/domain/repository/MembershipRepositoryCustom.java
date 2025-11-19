package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Membership;

public interface MembershipRepositoryCustom {

	Optional<Membership> activate(String brandId, String id);
	
	Optional<Membership> deactivate(String brandId, String id);
}
