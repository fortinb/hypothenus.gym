package com.iso.hypo.gym.repository;

import java.util.Optional;

import com.iso.hypo.gym.domain.aggregate.Course;

public interface CourseRepositoryCustom {

	Optional<Course> activate(String brandId, String gymId, String id);
	
	Optional<Course> deactivate(String brandId, String gymId, String id);
}
