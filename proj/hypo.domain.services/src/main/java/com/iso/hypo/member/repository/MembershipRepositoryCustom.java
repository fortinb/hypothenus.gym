package com.iso.hypo.member.repository;

import java.util.Optional;

import com.iso.hypo.member.domain.aggregate.Membership;

public interface MembershipRepositoryCustom {

	Optional<Membership> activate(String brandId, String id);
	
	Optional<Membership> deactivate(String brandId, String id);
}
