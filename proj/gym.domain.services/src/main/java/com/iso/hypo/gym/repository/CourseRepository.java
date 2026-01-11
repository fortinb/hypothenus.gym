package com.iso.hypo.gym.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.iso.hypo.gym.domain.aggregate.Course;

public interface CourseRepository extends PagingAndSortingRepository<Course, String>, CrudRepository<Course, String>, CourseRepositoryCustom {
	
	Optional<Course> findByBrandIdAndGymIdAndIdAndIsDeletedIsFalse(String brandId, String gymId, String id);
	
	Optional<Course> findByBrandIdAndGymIdAndCodeAndIsDeletedIsFalse(String brandId, String gymId, String code);
	
	Page<Course> findAllByBrandIdAndGymIdAndIsDeletedIsFalse(String brandId, String gymId, Pageable pageable);
	
	Page<Course> findAllByBrandIdAndGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(String brandId, String gymId, Pageable pageable);
	
	
}