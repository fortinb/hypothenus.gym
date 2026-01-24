package com.iso.hypo.services;

import com.iso.hypo.domain.dto.CourseDto;
import com.iso.hypo.services.exception.CourseException;

public interface CourseService {

    CourseDto create(CourseDto courseDto) throws CourseException;

    CourseDto update(CourseDto courseDto) throws CourseException;

    CourseDto patch(CourseDto courseDto) throws CourseException;

    void delete(String brandUuid, String gymUuid, String courseUuid) throws CourseException;

    CourseDto activate(String brandUuid, String gymUuid, String courseUuid) throws CourseException;

    CourseDto deactivate(String brandUuid, String gymUuid, String courseUuid) throws CourseException;
}


