package com.iso.hypo.membership.infrastructure.persistence.repository;

import java.util.Optional;

import com.iso.hypo.membership.infrastructure.persistence.entity.MembershipDocument;

public interface MembershipMongoRepositoryCustom {
	
	Optional<MembershipDocument> findByMembershipPlanUuid(String brandUuid, String memberUuid, String membershipPlanUuid);

    void deleteAll();
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}