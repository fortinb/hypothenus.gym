package com.iso.hypo.repositories;

import java.util.Optional;

import com.iso.hypo.domain.aggregate.Course;

public interface CourseRepositoryCustom {

	Optional<Course> activate(String brandUuid, String courseUuid);
	
	Optional<Course> deactivate(String brandUuid, String courseUuid);
	
	void delete(String brandUuid, String courseUuid, String deletedBy);
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}
