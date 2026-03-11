package com.iso.hypo.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.domain.dto.CourseDto;
import com.iso.hypo.services.exception.CourseException;

public interface CourseQueryService {

    void assertExists(String brandUuid, String courseUuid) throws CourseException; 

    CourseDto find(String brandUuid, String courseUuid) throws CourseException;

    Page<CourseDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws CourseException;
}