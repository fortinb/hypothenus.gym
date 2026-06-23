package com.iso.hypo.membership.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.iso.hypo.membership.infrastructure.persistence.entity.MembershipDocument;

public interface MembershipMongoRepository extends MongoRepository<MembershipDocument, String>, MembershipMongoRepositoryCustom {

	Optional<MembershipDocument> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String membershipUuid);
	
	Page<MembershipDocument> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<MembershipDocument> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);
	
	Page<MembershipDocument> findAllByBrandUuidAndMemberUuidAndDeletedIsFalse(String brandUuid, String memberUuid, Pageable pageable);
}