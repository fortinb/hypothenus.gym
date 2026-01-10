package com.iso.hypo.gym.domain.repository;

import java.util.Optional;

import com.iso.hypo.gym.domain.model.aggregate.Membership;

public interface MembershipRepositoryCustom {

	Optional<Membership> activate(String brandId, String id);
	
	Optional<Membership> deactivate(String brandId, String id);
}
