package com.iso.hypo.brand.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.iso.hypo.brand.infrastructure.persistence.entity.GymDocument;

public interface GymMongoRepository extends MongoRepository<GymDocument, String>, GymMongoRepositoryCustom {
    
	Optional<GymDocument> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String gymUuid);
	
	Optional<GymDocument> findByBrandUuidAndCode(String brandUuid, String code);
	
	Page<GymDocument> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<GymDocument> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);
}