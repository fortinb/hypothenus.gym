package com.iso.hypo.brand.application.usecase.impl;

import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iso.hypo.brand.application.dto.CourseDto;
import com.iso.hypo.brand.application.exception.CourseException;
import com.iso.hypo.brand.application.mapper.CourseDtoMapper;
import com.iso.hypo.brand.application.usecase.CourseQueryService;
import com.iso.hypo.brand.domain.model.Course;
import com.iso.hypo.brand.domain.repository.CourseRepository;
import com.iso.hypo.common.application.context.RequestContext;
import com.iso.hypo.common.application.dto.PageResultDto;
import com.iso.hypo.common.domain.model.pagination.PageRequest;
import com.iso.hypo.common.domain.model.pagination.PageResult;

@Service
public class CourseQueryServiceImpl implements CourseQueryService {

	private final CourseRepository courseRepository;

	private final CourseDtoMapper courseMapper;

	private static final Logger logger = LoggerFactory.getLogger(CourseQueryServiceImpl.class);

	private final RequestContext requestContext;

	public CourseQueryServiceImpl(CourseDtoMapper courseMapper, CourseRepository courseRepository,
			RequestContext requestContext) {
		this.courseMapper = courseMapper;
		this.courseRepository = courseRepository;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public void assertExists(String brandUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_NOT_FOUND,
						"Course not found");
			}
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, courseUuid={}", brandUuid, courseUuid, e);

			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.FIND_FAILED, e);
		}
	}

	@Override
	public CourseDto find(String brandUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.findByBrandUuidAndUuidAndDeletedIsFalse(brandUuid, courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_NOT_FOUND,
						"Course not found");
			}

			return courseMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, courseUuid={}", brandUuid, courseUuid, e);

			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.FIND_FAILED, e);
		}
	}

	@Override
	public PageResultDto<CourseDto> list(String brandUuid, int page, int pageSize, boolean includeInactive)
			throws CourseException {
		try {
			PageRequest pageRequest = PageRequest.of(page, pageSize);
			PageResult<CourseDto> result = includeInactive
					? courseRepository.findAllByBrandUuidAndDeletedIsFalse(brandUuid, pageRequest)
							.map(courseMapper::toDto)
					: courseRepository.findAllByBrandUuidAndDeletedIsFalseAndActiveIsTrue(brandUuid, pageRequest)
							.map(courseMapper::toDto);
			return PageResultDto.from(result);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.FIND_FAILED, e);
		}
	}
}