package com.iso.hypo.model.repository;

import java.util.Optional;

import com.iso.hypo.model.aggregate.Membership;

public interface MembershipRepositoryCustom {

	Optional<Membership> activate(String brandUuid, String membershipUuid);
	
	Optional<Membership> deactivate(String brandUuid, String membershipUuid);
}
