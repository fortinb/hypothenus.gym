package com.iso.hypo.course.repository;

import java.util.Optional;

import com.iso.hypo.course.domain.aggregate.Course;

public interface CourseRepositoryCustom {

	Optional<Course> activate(String brandId, String gymId, String id);
	
	Optional<Course> deactivate(String brandId, String gymId, String id);
}
