package com.iso.hypo.brand.application.usecase;

import com.iso.hypo.brand.application.dto.CourseDto;
import com.iso.hypo.brand.application.exception.CourseException;
import com.iso.hypo.common.application.dto.PageResultDto;

public interface CourseQueryService {

    void assertExists(String brandUuid, String courseUuid) throws CourseException; 

    CourseDto find(String brandUuid, String courseUuid) throws CourseException;

    PageResultDto<CourseDto> list(String brandUuid, int page, int pageSize, boolean includeInactive) throws CourseException;
}