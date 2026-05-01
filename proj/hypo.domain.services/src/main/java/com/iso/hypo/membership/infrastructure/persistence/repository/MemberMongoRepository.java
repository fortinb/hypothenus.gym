package com.iso.hypo.membership.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.iso.hypo.membership.infrastructure.persistence.entity.MemberDocument;

public interface MemberMongoRepository extends MongoRepository<MemberDocument, String>, MemberMongoRepositoryCustom {

	Optional<MemberDocument> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String memberUuid);

	Page<MemberDocument> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);

	Page<MemberDocument> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);

	Optional<MemberDocument> findByBrandUuidAndPersonEmailAndDeletedIsFalse(String brandUuid, String email);

	Optional<MemberDocument> findByBrandUuidAndUserUuidAndDeletedIsFalse(String brandUuid, String userUuid);
}