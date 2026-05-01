package com.iso.hypo.brand.infrastructure.persistence.repository;

public interface CourseMongoRepositoryCustom {

	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
	
    void deleteAll();
}