package com.iso.hypo.brand.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.iso.hypo.brand.infrastructure.persistence.entity.CourseDocument;

public interface CourseMongoRepository extends MongoRepository<CourseDocument, String>, CourseMongoRepositoryCustom {
    
	Optional<CourseDocument> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String courseUuid);
	
	Optional<CourseDocument> findByBrandUuidAndCodeAndDeletedIsFalse(String brandUuid, String code);
	
	Page<CourseDocument> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<CourseDocument> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);
}