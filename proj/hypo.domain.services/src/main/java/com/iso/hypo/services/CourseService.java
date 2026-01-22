package com.iso.hypo.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.domain.dto.CourseDto;
import com.iso.hypo.services.exception.CourseException;

public interface CourseService {

    CourseDto create(String brandUuid, String gymUuid, CourseDto courseDto) throws CourseException;

    CourseDto update(String brandUuid, String gymUuid, CourseDto courseDto) throws CourseException;

    CourseDto patch(String brandUuid, String gymUuid, CourseDto courseDto) throws CourseException;

    void delete(String brandUuid, String gymUuid, String courseUuid) throws CourseException;

    CourseDto findByCourseUuid(String brandUuid, String gymUuid, String courseUuid) throws CourseException;

    Page<CourseDto> list(String brandUuid, String gymUuid, int page, int pageSize, boolean includeInactive) throws CourseException;

    CourseDto activate(String brandUuid, String gymUuid, String courseUuid) throws CourseException;

    CourseDto deactivate(String brandUuid, String gymUuid, String courseUuid) throws CourseException;
}


