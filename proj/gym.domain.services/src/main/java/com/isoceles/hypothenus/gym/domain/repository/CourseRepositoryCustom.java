package com.isoceles.hypothenus.gym.domain.repository;

import java.util.Optional;

import com.isoceles.hypothenus.gym.domain.model.aggregate.Course;

public interface CourseRepositoryCustom {

	Optional<Course> activate(String gymId, String id);
	
	Optional<Course> deactivate(String gymId, String id);
}
