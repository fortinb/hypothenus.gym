package com.iso.hypo.model.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.model.dto.CourseDto;
import com.iso.hypo.model.exception.GymException;

public interface CourseService {

    CourseDto create(String brandUuid, String gymUuid, CourseDto courseDto) throws GymException;

    CourseDto update(String brandUuid, String gymUuid, CourseDto courseDto) throws GymException;

    CourseDto patch(String brandUuid, String gymUuid, CourseDto courseDto) throws GymException;

    void delete(String brandUuid, String gymUuid, String courseUuid) throws GymException;

    CourseDto findByCourseUuid(String brandUuid, String gymUuid, String courseUuid) throws GymException;

    Page<CourseDto> list(String brandUuid, String gymUuid, int page, int pageSize, boolean includeInactive) throws GymException;

    CourseDto activate(String brandUuid, String gymUuid, String courseUuid) throws GymException;

    CourseDto deactivate(String brandUuid, String gymUuid, String courseUuid) throws GymException;
}