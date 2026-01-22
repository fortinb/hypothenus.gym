package com.iso.hypo.repositories;

import java.util.Optional;

import com.iso.hypo.domain.aggregate.Course;

public interface CourseRepositoryCustom {

	Optional<Course> activate(String brandUuid, String gymUuid, String courseUuid);
	
	Optional<Course> deactivate(String brandUuid, String gymUuid, String courseUuid);
}

