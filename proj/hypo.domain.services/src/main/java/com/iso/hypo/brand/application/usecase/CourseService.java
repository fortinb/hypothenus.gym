package com.iso.hypo.brand.application.usecase;

import com.iso.hypo.brand.application.dto.CourseDto;
import com.iso.hypo.brand.domain.exception.CourseException;

public interface CourseService {

    CourseDto create(CourseDto courseDto) throws CourseException;

    CourseDto update(CourseDto courseDto) throws CourseException;

    CourseDto patch(CourseDto courseDto) throws CourseException;

    void delete(String brandUuid, String courseUuid) throws CourseException;

    CourseDto activate(String brandUuid, String courseUuid) throws CourseException;

    CourseDto deactivate(String brandUuid, String courseUuid) throws CourseException;
    
    void deleteAllByBrandUuid(String brandUuid) throws CourseException;
}