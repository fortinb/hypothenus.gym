package com.iso.hypo.brand.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.iso.hypo.brand.infrastructure.persistence.entity.CoachDocument;

public interface CoachMongoRepository extends MongoRepository<CoachDocument, String>, CoachMongoRepositoryCustom {
    
	Optional<CoachDocument> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String coachUuid);
	
	Page<CoachDocument> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<CoachDocument> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);

}