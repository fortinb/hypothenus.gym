package com.iso.hypo.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.domain.aggregate.Course;

public interface CourseRepository extends PagingAndSortingRepository<Course, String>, CrudRepository<Course, String>, CourseRepositoryCustom {
	
	Optional<Course> findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(String brandUuid, String gymUuid, String courseUuid);
	
	Optional<Course> findByBrandUuidAndGymUuidAndCodeAndIsDeletedIsFalse(String brandUuid, String gymUuid, String code);
	
	Page<Course> findAllByBrandUuidAndGymUuidAndIsDeletedIsFalse(String brandUuid, String gymUuid, Pageable pageable);
	
	Page<Course> findAllByBrandUuidAndGymUuidAndIsDeletedIsFalseAndIsActiveIsTrue(String brandUuid, String gymUuid, Pageable pageable);
	
	Page<Course> findAllByBrandUuidAndIsDeletedIsFalse(String brandUuid, Pageable pageable);
}

