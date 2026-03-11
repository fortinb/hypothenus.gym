package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.domain.aggregate.Course;

public interface CourseRepository extends PagingAndSortingRepository<Course, String>, CrudRepository<Course, String>, CourseRepositoryCustom {
	
	Optional<Course> findByBrandUuidAndUuidAndIsDeletedIsFalse(String brandUuid, String courseUuid);
	
	Optional<Course> findByBrandUuidAndCodeAndIsDeletedIsFalse(String brandUuid, String code);
	
	Page<Course> findAllByBrandUuidAndIsDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<Course> findAllByBrandUuidAndIsDeletedIsFalseAndIsActiveIsTrue(String brandUuid, Pageable pageable);
}