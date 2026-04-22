package com.iso.hypo.membership.domain.repository;

import java.util.Optional;

import com.iso.hypo.membership.domain.model.Membership;

public interface MembershipRepositoryCustom {

	Optional<Membership> activate(String brandUuid, String membershipUuid);
	
	Optional<Membership> deactivate(String brandUuid, String membershipUuid);
	
	void delete(String brandUuid, String membershipUuid, String deletedBy);
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}

