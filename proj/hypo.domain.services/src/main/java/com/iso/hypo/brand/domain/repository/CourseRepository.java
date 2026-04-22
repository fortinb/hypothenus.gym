package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.brand.domain.model.Course;

public interface CourseRepository extends PagingAndSortingRepository<Course, String>, CrudRepository<Course, String>, CourseRepositoryCustom {
	
	Optional<Course> findByBrandUuidAndUuidAndDeletedIsFalse(String brandUuid, String courseUuid);
	
	Optional<Course> findByBrandUuidAndCodeAndDeletedIsFalse(String brandUuid, String code);
	
	Page<Course> findAllByBrandUuidAndDeletedIsFalse(String brandUuid, Pageable pageable);
	
	Page<Course> findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(String brandUuid, Pageable pageable);
}