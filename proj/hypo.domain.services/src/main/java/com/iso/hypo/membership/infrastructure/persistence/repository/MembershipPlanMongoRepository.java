package com.iso.hypo.membership.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.iso.hypo.membership.infrastructure.persistence.entity.MembershipPlanDocument;

public interface MembershipPlanMongoRepository extends MongoRepository<MembershipPlanDocument, String>, MembershipPlanMongoRepositoryCustom {

	Optional<MembershipPlanDocument> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String membershipPlanUuid);
	
	Page<MembershipPlanDocument> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<MembershipPlanDocument> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);
	
}