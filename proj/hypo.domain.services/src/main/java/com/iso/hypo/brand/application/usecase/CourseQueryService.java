package com.iso.hypo.brand.application.usecase;

import org.springframework.data.domain.Page;

import com.iso.hypo.brand.application.dto.CourseDto;
import com.iso.hypo.brand.domain.exception.CourseException;

public interface CourseQueryService {

    void assertExists(String brandUuid, String courseUuid) throws CourseException; 

    CourseDto find(String brandUuid, String courseUuid) throws CourseException;

    Page<CourseDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws CourseException;
}