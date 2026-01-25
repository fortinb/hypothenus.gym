package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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

	private final GymQueryService gymQueryService;

	private final CoachRepository coachRepository;

	private final CourseRepository courseRepository;

	private final CourseMapper courseMapper;

	private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);
	
	private final RequestContext requestContext;

	public CourseServiceImpl(CourseMapper courseMapper, CoachRepository coachRepository, CourseRepository courseRepository, GymQueryService gymQueryService, RequestContext requestContext) {
		this.courseMapper = courseMapper;
		this.coachRepository = coachRepository;
		this.courseRepository = courseRepository;
		this.gymQueryService = gymQueryService;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	public CourseDto create(CourseDto courseDto) throws CourseException {
		try {
			Assert.notNull(courseDto, "courseDto must not be null");
			Course course = courseMapper.toEntity(courseDto);
			
			gymQueryService.assertExists(course.getBrandUuid(), course.getGymUuid());

			Optional<Course> existingCourse = courseRepository
					.findByBrandUuidAndGymUuidAndCodeAndIsDeletedIsFalse(course.getBrandUuid(), course.getGymUuid(), course.getCode());
			if (existingCourse.isPresent()) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_CODE_ALREADY_EXIST, "Duplicate course code");
			}

			// Validate coaches
			if (course.getCoachs() != null) {
				for (Coach coach : course.getCoachs()) {
					Optional<Coach> existingCoach = coachRepository
							.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(course.getBrandUuid(), course.getGymUuid(), coach.getUuid());
					if (existingCoach.isEmpty()) {
						throw new CourseException(requestContext.getTrackingNumber(), CourseException.COACH_NOT_FOUND, "Coach not found: " + coach.getUuid());
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
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}", courseDto.getBrandUuid(), courseDto.getGymUuid(), courseDto.getUuid(), e);
			
			if (e instanceof GymException) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.GYM_NOT_FOUND, "Gym not found");
			}
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.CREATION_FAILED, e);
		}
	}

	@Override
	public CourseDto update(CourseDto courseDto) throws CourseException {
		try {
			Assert.notNull(courseDto, "courseDto must not be null");
			Course course = courseMapper.toEntity(courseDto);
			
			Course oldCourse = this.readByCourseUuid(course.getBrandUuid(), course.getGymUuid(), course.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(false).setCollectionsMergeEnabled(false);

			mapper = courseMapper.initCourseMappings(mapper);
			mapper.map(course, oldCourse);

			// Validate coaches
			if (oldCourse.getCoachs() != null) {
				for (Coach coach : oldCourse.getCoachs()) {
					Optional<Coach> existingCoach = coachRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(course.getBrandUuid(), course.getGymUuid(), coach.getUuid());
					if (existingCoach.isEmpty()) {
						throw new CourseException(requestContext.getTrackingNumber(), CourseException.COACH_NOT_FOUND, "Coach not found: " + coach.getUuid());
					}
					coach.setId(existingCoach.get().getId());
				}
			}

			oldCourse.setModifiedOn(Instant.now());
			oldCourse.setModifiedBy(requestContext.getUsername());

			Course saved = courseRepository.save(oldCourse);
			return courseMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}", courseDto.getBrandUuid(), courseDto.getGymUuid(), courseDto.getUuid(), e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.UPDATE_FAILED, e);
		}
	}

	@Override
	public CourseDto patch(CourseDto courseDto) throws CourseException {
		try {
			Assert.notNull(courseDto, "courseDto must not be null");
			Course course = courseMapper.toEntity(courseDto);

			Course oldCourse = this.readByCourseUuid(course.getBrandUuid(), course.getGymUuid(), course.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(false).setCollectionsMergeEnabled(false);

			mapper = courseMapper.initCourseMappings(mapper);
			mapper.map(course, oldCourse);

			// Validate coaches
			if (oldCourse.getCoachs() != null) {
				for (Coach coach : oldCourse.getCoachs()) {
					Optional<Coach> existingCoach = coachRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(course.getBrandUuid(), course.getGymUuid(), coach.getUuid());
					if (existingCoach.isEmpty()) {
						throw new CourseException(requestContext.getTrackingNumber(), CourseException.COACH_NOT_FOUND, "Coach not found: " + coach.getUuid());
					}
					coach.setId(existingCoach.get().getId());
				}
			}

			oldCourse.setModifiedOn(Instant.now());
			oldCourse.setModifiedBy(requestContext.getUsername());

			Course saved = courseRepository.save(oldCourse);
			return courseMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}", courseDto.getBrandUuid(), courseDto.getGymUuid(), courseDto.getUuid(), e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.UPDATE_FAILED, e);
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
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}", brandUuid, gymUuid, courseUuid, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.DELETE_FAILED, e);
		}
	}

	@Override
	public CourseDto activate(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.activate(brandUuid, gymUuid, courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_NOT_FOUND, "Course not found");
			}

			return courseMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}", brandUuid, gymUuid, courseUuid, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	public CourseDto deactivate(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.deactivate(brandUuid, gymUuid, courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_NOT_FOUND, "Course not found");
			}

			return courseMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, gymUuid={}, courseUuid={}", brandUuid, gymUuid, courseUuid, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.DEACTIVATION_FAILED, e);
		}
	}

	private Course readByCourseUuid(String brandUuid, String gymUuid, String courseUuid) throws CourseException {
        Optional<Course> entity = courseRepository.findByBrandUuidAndGymUuidAndUuidAndIsDeletedIsFalse(brandUuid,
                    gymUuid, courseUuid);
		if (entity.isEmpty()) {
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_NOT_FOUND, "Course not found");
		}

		return entity.get();
	}
}