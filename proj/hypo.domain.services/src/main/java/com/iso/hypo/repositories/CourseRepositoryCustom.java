package com.iso.hypo.repositories;

import java.util.Optional;

import com.iso.hypo.domain.aggregate.Course;

public interface CourseRepositoryCustom {

	Optional<Course> activate(String brandUuid, String gymUuid, String courseUuid);
	
	Optional<Course> deactivate(String brandUuid, String gymUuid, String courseUuid);
	
	void delete(String brandUuid, String gymUuid, String courseUuid, String deletedBy);
	
	long deleteAllByGymUuid(String brandUuid, String gymUuid, String deletedBy);
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}

