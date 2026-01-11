package com.iso.hypo.gym.services;

import org.springframework.data.domain.Page;

import com.iso.hypo.gym.dto.CourseDto;
import com.iso.hypo.gym.exception.GymException;

public interface CourseService {

    CourseDto create(String brandId, String gymId, CourseDto courseDto) throws GymException;

    CourseDto update(String brandId, String gymId, CourseDto courseDto) throws GymException;

    CourseDto patch(String brandId, String gymId, CourseDto courseDto) throws GymException;

    void delete(String brandId, String gymId, String courseId) throws GymException;

    CourseDto findByCourseId(String brandId, String gymId, String id) throws GymException;

    Page<CourseDto> list(String brandId, String gymId, int page, int pageSize, boolean includeInactive) throws GymException;

    CourseDto activate(String brandId, String gymId, String id) throws GymException;

    CourseDto deactivate(String brandId, String gymId, String id) throws GymException;
}