package com.iso.hypo.brand.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.iso.hypo.brand.infrastructure.persistence.entity.UserDocument;

public interface UserMongoRepository extends MongoRepository<UserDocument, String>, UserMongoRepositoryCustom {

	Optional<UserDocument> findByEmailAndDeletedIsFalse(String email);

	Optional<UserDocument> findByIdpIdAndDeletedIsFalse(String idpId);

	Optional<UserDocument> findByUuidAndDeletedIsFalse(String userUuid);

	Page<UserDocument> findAllByDeletedIsFalse(Pageable pageable);
	
	Page<UserDocument> findAllByDeletedIsFalseAndActiveIsTrue(Pageable pageable);
}