package com.iso.hypo.brand.domain.repository;

import java.util.Optional;

import com.iso.hypo.brand.domain.model.Course;

public interface CourseRepositoryCustom {

	Optional<Course> activate(String brandUuid, String courseUuid);
	
	Optional<Course> deactivate(String brandUuid, String courseUuid);
	
	void delete(String brandUuid, String courseUuid, String deletedBy);
	
	long deleteAllByBrandUuid(String brandUuid, String deletedBy);
}
