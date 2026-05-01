package com.iso.hypo.brand.infrastructure.persistence.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.iso.hypo.brand.infrastructure.persistence.entity.BrandDocument;

public interface BrandMongoRepository extends MongoRepository<BrandDocument, String>, BrandMongoRepositoryCustom {

    Optional<BrandDocument> findByUuidAndDeletedIsFalse(String brandUuid);

    Optional<BrandDocument> findByCode(String code);

    Optional<BrandDocument> findByCodeAndDeletedIsFalse(String code);

    Page<BrandDocument> findAllByDeletedIsFalse(Pageable pageable);

    Page<BrandDocument> findAllByDeletedIsFalseAndActiveIsTrue(Pageable pageable);
}