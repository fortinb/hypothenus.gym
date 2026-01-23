package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.dto.CourseDto;
import com.iso.hypo.repositories.CoachRepository;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.services.CourseService;
import com.iso.hypo.services.GymQueryService;
import com.iso.hypo.services.exception.CourseException;
import com.iso.hypo.services.exception.GymException;
import com.iso.hypo.services.mappers.CourseMapper;

@Service
public class CourseServiceImpl implements CourseService {

	private GymQueryService gymQueryService;

	private CoachRepository coachRepository;

	private CourseRepository courseRepository;

	private CourseMapper courseMapper;

	@Autowired
	private Logger logger;
	
	@Autowired
	private RequestContext requestContext;

	public CourseServiceImpl(GymQueryService gymQueryService, CoachRepository coachRepository, CourseRepository courseRepository, CourseMapper courseMapper) {
		this.gymQueryService = gymQueryService;
		this.coachRepository = coachRepository;
		this.courseRepository = courseRepository;
		this.courseMapper = courseMapper;
	}

	@Override
	public CourseDto create(String brandUuid, String gymUuid, CourseDto courseDto) throws CourseException {
		try {
			gymQueryService.assertExists(brandUuid, gymUuid);

			Course course = courseMapper.toEntity(courseDto);
			if (!course.getBrandUuid().equals(brandUuid)) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.INVALID_BRAND, "Invalid brand");
			}

			if (!course.getGymUuid().equals(gymUuid)) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.INVALID_GYM, "Invalid gym");
			}

			Optional<Course> existingCourse = courseRepository
					.findByBrandUuidAndGymUuidAndCodeAndIsDeletedIsFalse(brandUuid, gymUuid, course.getCode());
			if (existingCourse.isPresent()) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.COURSE_CODE_ALREADY_EXIST, "Duplicate course code");
			}

			// Validate coaches
			if (course.getCoachs() != null) {
				for (Coach coach : course.getCoachs()) {
					Optional<Coach> existingCoach = coachRepository
							.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid, coach.getUuid());
					if (existingCoach.isEmpty()) {
						throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.COACH_NOT_FOUND, "Coach not found: " + coach.getUuid());
					}
					coach.setId(existingCoach.get().getId());
				}
			}

			course.setUuid(UUID.randomUUID().toString());
			course.setCreatedOn(Instant.now());
			course.setCreatedBy(requestContext.getUsername());

			Course saved = courseRepository.save(course);
			return courseMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}, trackingNumber={}", brandUuid, gymUuid, courseDto != null ? courseDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof GymException) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.GYM_NOT_FOUND, "Gym not found");
			}
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.CREATION_FAILED, e);
		}
	}

	@Override
	public CourseDto update(String brandUuid, String gymUuid, CourseDto courseDto) throws CourseException {
		try {
			Course course = courseMapper.toEntity(courseDto);
			if (!course.getBrandUuid().equals(brandUuid)) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.INVALID_BRAND, "Invalid brand");
			}

			if (!course.getGymUuid().equals(gymUuid)) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.INVALID_GYM, "Invalid gym");
			}

			Course oldCourse = this.readByCourseUuid(brandUuid, gymUuid, course.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(false).setCollectionsMergeEnabled(false);

			mapper = courseMapper.initCourseMappings(mapper);
			mapper.map(course, oldCourse);

			// Validate coaches
			if (oldCourse.getCoachs() != null) {
				for (Coach coach : oldCourse.getCoachs()) {
					Optional<Coach> existingCoach = coachRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid, coach.getUuid());
					if (existingCoach.isEmpty()) {
						throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.COACH_NOT_FOUND, "Coach not found: " + coach.getUuid());
					}
					coach.setId(existingCoach.get().getId());
				}
			}

			oldCourse.setModifiedOn(Instant.now());
			oldCourse.setModifiedBy(requestContext.getUsername());

			Course saved = courseRepository.save(oldCourse);
			return courseMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}, trackingNumber={}", brandUuid, gymUuid, courseDto != null ? courseDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.UPDATE_FAILED, e);
		}
	}

	@Override
	public CourseDto patch(String brandUuid, String gymUuid, CourseDto courseDto) throws CourseException {
		try {
			Course course = courseMapper.toEntity(courseDto);
			if (!course.getBrandUuid().equals(brandUuid)) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.INVALID_BRAND, "Invalid brand");
			}

			if (!course.getGymUuid().equals(gymUuid)) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.INVALID_GYM, "Invalid gym");
			}

			Course oldCourse = this.readByCourseUuid(brandUuid, gymUuid, course.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(false).setCollectionsMergeEnabled(false);

			mapper = courseMapper.initCourseMappings(mapper);
			mapper.map(course, oldCourse);

			// Validate coaches
			if (oldCourse.getCoachs() != null) {
				for (Coach coach : oldCourse.getCoachs()) {
					Optional<Coach> existingCoach = coachRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid, gymUuid, coach.getUuid());
					if (existingCoach.isEmpty()) {
						throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.COACH_NOT_FOUND, "Coach not found: " + coach.getUuid());
					}
					coach.setId(existingCoach.get().getId());
				}
			}

			oldCourse.setModifiedOn(Instant.now());
			oldCourse.setModifiedBy(requestContext.getUsername());

			Course saved = courseRepository.save(oldCourse);
			return courseMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}, trackingNumber={}", brandUuid, gymUuid, courseDto != null ? courseDto.getUuid() : null, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.UPDATE_FAILED, e);
		}
	}

	@Override
	public void delete(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
		try {
			Course entity = this.readByCourseUuid(brandUuid, gymUuid, courseUuid);
			entity.setDeleted(true);
	
			entity.setDeletedOn(Instant.now());
			entity.setDeletedBy(requestContext.getUsername());
	
			courseRepository.save(entity);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}, trackingNumber={}", brandUuid, gymUuid, courseUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.DELETE_FAILED, e);
		}
	}

	@Override
	public CourseDto activate(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.activate(brandUuid, gymUuid, courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.COURSE_NOT_FOUND, "Course not found");
			}

			return courseMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}, trackingNumber={}", brandUuid, gymUuid, courseUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	public CourseDto deactivate(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.deactivate(brandUuid, gymUuid, courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.COURSE_NOT_FOUND, "Course not found");
			}

			return courseMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}, trackingNumber={}", brandUuid, gymUuid, courseUuid, requestContext != null ? requestContext.getTrackingNumber() : null, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.DEACTIVATION_FAILED, e);
		}
	}

	private Course readByCourseUuid(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
		Optional<Course> entity = courseRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid,
					gymUuid, courseUuid);
		if (entity.isEmpty()) {
			throw new CourseException(requestContext != null ? requestContext.getTrackingNumber() : null, CourseException.COURSE_NOT_FOUND, "Course not found");
		}

		return entity.get();
	}
}