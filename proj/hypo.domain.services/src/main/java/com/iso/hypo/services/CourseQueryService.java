package com.iso.hypo.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.domain.dto.CourseDto;
import com.iso.hypo.services.exception.CourseException;

public interface CourseQueryService {

    void assertExists(String brandUuid, String gymUuid, String courseUuid) throws CourseException; 

    CourseDto find(String brandUuid, String gymUuid, String courseUuid) throws CourseException;

    Page<CourseDto> list(String brandUuid, String gymUuid, int page, int pageSize, boolean includeInactive) throws CourseException;
}


