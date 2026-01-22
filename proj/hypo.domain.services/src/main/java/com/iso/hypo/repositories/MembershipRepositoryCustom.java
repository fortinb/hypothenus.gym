package com.iso.hypo.repositories;

import java.util.Optional;

import com.iso.hypo.domain.aggregate.Membership;

public interface MembershipRepositoryCustom {

	Optional<Membership> activate(String brandUuid, String membershipUuid);
	
	Optional<Membership> deactivate(String brandUuid, String membershipUuid);
}

