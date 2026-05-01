package com.iso.hypo.brand.infrastructure.persistence.repository;

public interface CoachMongoRepositoryCustom {

    void deleteAll();

	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}