package com.iso.hypo.membership.infrastructure.persistence.repository;

public interface MembershipMongoRepositoryCustom {

    void deleteAll();
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}