package com.iso.hypo.membership.repository;

import java.util.Optional;

import com.iso.hypo.membership.domain.aggregate.Membership;

public interface MembershipRepositoryCustom {

	Optional<Membership> activate(String brandId, String id);
	
	Optional<Membership> deactivate(String brandId, String id);
}
