package com.iso.hypo.services.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.dto.CourseDto;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.services.CourseQueryService;
import com.iso.hypo.services.exception.CourseException;
import com.iso.hypo.services.mappers.CourseMapper;

@Service
public class CourseQueryServiceImpl implements CourseQueryService {

	private CourseRepository courseRepository;

	private CourseMapper courseMapper;

	@Autowired
	private Logger logger;

	@Autowired
	private RequestContext requestContext;

	public CourseQueryServiceImpl(CourseRepository courseRepository, CourseMapper courseMapper) {
		this.courseRepository = courseRepository;
		this.courseMapper = courseMapper;
	}

	@Override
	public void assertExists(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid,
					courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.COURSE_NOT_FOUND, "Course not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}, trackingNumber={}", brandUuid, gymUuid, courseUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.FIND_FAILED, e);
		}
	}
	
	@Override
	public CourseDto find(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid,
					courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.COURSE_NOT_FOUND, "Course not found");
			}

			return courseMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}, trackingNumber={}", brandUuid, gymUuid, courseUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.FIND_FAILED, e);
		}
	}

	@Override
	public Page<CourseDto> list(String brandUuid, String gymUuid, int page, int pageSize, boolean includeInactive) throws CourseException {
		try {
			if (includeInactive) {
				return courseRepository
						.findAllByBrandUuidAndGymUuidAndIsDeletedIsFalse(brandUuid, gymUuid,
							PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"))
						.map(c -> courseMapper.toDto(c));
			}

			return courseRepository.findAllByBrandUuidAndGymUuidAndIsDeletedIsFalseAndIsActiveIsTrue(brandUuid, gymUuid,
					PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname")).map(c -> courseMapper.toDto(c));
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, trackingNumber={}", brandUuid, gymUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.FIND_FAILED, e);
		}
	}
}