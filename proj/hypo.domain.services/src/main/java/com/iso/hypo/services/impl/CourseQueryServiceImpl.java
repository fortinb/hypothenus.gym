package com.iso.hypo.services.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private final CourseRepository courseRepository;

	private final CourseMapper courseMapper;

	private static final Logger logger = LoggerFactory.getLogger(CourseQueryServiceImpl.class);

	private final RequestContext requestContext;

	public CourseQueryServiceImpl(CourseMapper courseMapper, CourseRepository courseRepository, RequestContext requestContext) {
		this.courseMapper = courseMapper;
		this.courseRepository = courseRepository;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public void assertExists(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid,
						courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_NOT_FOUND, "Course not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}", brandUuid, gymUuid, courseUuid, e);
			
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.FIND_FAILED, e);
		}
	}
	
	@Override
	public CourseDto find(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid,
						courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_NOT_FOUND, "Course not found");
			}

			return courseMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}", brandUuid, gymUuid, courseUuid, e);
			
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.FIND_FAILED, e);
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
			logger.error("Error - brandUuid={}, gymUuid={}", brandUuid, gymUuid, e);
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.FIND_FAILED, e);
		}
	}
}