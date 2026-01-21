package com.iso.hypo.model.repository;

import java.util.Optional;

import com.iso.hypo.model.aggregate.Course;

public interface CourseRepositoryCustom {

	Optional<Course> activate(String brandUuid, String gymUuid, String courseUuid);
	
	Optional<Course> deactivate(String brandUuid, String gymUuid, String courseUuid);
}
