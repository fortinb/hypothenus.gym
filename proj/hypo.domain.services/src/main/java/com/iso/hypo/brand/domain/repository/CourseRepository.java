package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import com.iso.hypo.brand.domain.model.Course;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

public interface CourseRepository {
	
	Optional<Course> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String courseUuid);
	
	Optional<Course> findByBrandUuidAndCodeAndDeletedIsFalse(String brandUuid, String code);
	
	PageResult<Course> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, PageRequest pageRequest);
	
	PageResult<Course> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, PageRequest pageRequest);
	
	Course save(Course course);

    void delete(Course course);

    void deleteAll();
    
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}