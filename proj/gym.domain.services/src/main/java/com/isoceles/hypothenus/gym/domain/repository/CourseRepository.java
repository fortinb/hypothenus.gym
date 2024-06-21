package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Course;

public interface CourseRepository extends PagingAndSortingRepository<Course, String>, CrudRepository<Course, String>, CourseRepositoryCustom {
	
	Optional<Course> findByGymIdAndIdAndIsDeletedIsFalse(String gymId, String id);
	
	Page<Course> findAllByGymIdAndIsDeletedIsFalse(String gymId, Pageable pageable);
	
	Page<Course> findAllByGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(String gymId, Pageable pageable);
	
	
}