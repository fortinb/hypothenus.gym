package com.iso.hypo.services.impl;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.Message;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.dto.CourseDto;
import com.iso.hypo.domain.enumeration.MessageSeverityEnum;
import com.iso.hypo.events.event.OperationEnum;
import com.iso.hypo.repositories.CourseRepository;
import com.iso.hypo.services.BrandQueryService;
import com.iso.hypo.services.CourseService;
import com.iso.hypo.services.event.CourseEvent;
import com.iso.hypo.services.exception.BrandException;
import com.iso.hypo.services.exception.CourseException;
import com.iso.hypo.services.mappers.CourseMapper;

@Service
public class CourseServiceImpl implements CourseService {

	private final BrandQueryService brandQueryService;

	private final CourseRepository courseRepository;

	private final ApplicationEventPublisher eventPublisher;
	
	private final CourseMapper courseMapper;

	private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);
	
	private final RequestContext requestContext;

	public CourseServiceImpl(
			CourseMapper courseMapper, 
			CourseRepository courseRepository, 
			BrandQueryService brandQueryService, 
			ApplicationEventPublisher eventPublisher,
			RequestContext requestContext) {
		this.courseMapper = courseMapper;
		this.courseRepository = courseRepository;
		this.brandQueryService = brandQueryService;
		this.eventPublisher = eventPublisher;
		this.requestContext = Objects.requireNonNull(requestContext, "requestContext must not be null");
	}

	@Override
	@Transactional
	public CourseDto create(CourseDto courseDto) throws CourseException {
		try {
			Assert.notNull(courseDto, "courseDto must not be null");
			Course course = courseMapper.toEntity(courseDto);
			
			brandQueryService.assertExists(course.getBrandUuid());

			Optional<Course> existingCourse = courseRepository
					.findByBrandUuidAndCodeAndIsDeletedIsFalse(course.getBrandUuid(), course.getCode());
			if (existingCourse.isPresent()) {
				Message message = new Message();
				message.setCode(CourseException.COURSE_CODE_ALREADY_EXIST);
				message.setDescription("Duplicate course code");
				message.setSeverity(MessageSeverityEnum.warning);
				existingCourse.get().getMessages().add(message);

				throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_CODE_ALREADY_EXIST, "Duplicate course code", courseMapper.toDto(existingCourse.get()));
			}

			course.setUuid(UUID.randomUUID().toString());
			course.setCreatedOn(Instant.now());
			course.setCreatedBy(requestContext.getUsername());

			Course saved = courseRepository.save(course);
			return courseMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, courseUuid={}", courseDto.getBrandUuid(), courseDto.getUuid(), e);
			
			if (e instanceof BrandException) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.BRAND_NOT_FOUND, "Brand not found");
			}
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.CREATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public CourseDto update(CourseDto courseDto) throws CourseException {
		try {
			return updateCourse(courseDto, false);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, courseUuid={}", courseDto.getBrandUuid(), courseDto.getUuid(), e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public CourseDto patch(CourseDto courseDto) throws CourseException {
		try {
			return updateCourse(courseDto, true);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, courseUuid={}", courseDto.getBrandUuid(), courseDto.getUuid(), e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.UPDATE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public CourseDto activate(String brandUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.activate(brandUuid, courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_NOT_FOUND, "Course not found");
			}
			return courseMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, courseUuid={}", brandUuid, courseUuid, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.ACTIVATION_FAILED, e);
		}
	}

	@Override
	@Transactional
	public CourseDto deactivate(String brandUuid, String courseUuid) throws CourseException {
		try {
			Optional<Course> entity = courseRepository.deactivate(brandUuid, courseUuid);
			if (entity.isEmpty()) {
				throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_NOT_FOUND, "Course not found");
			}
			return courseMapper.toDto(entity.get());
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, courseUuid={}", brandUuid, courseUuid, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.DEACTIVATION_FAILED, e);
		}
	}
	
	@Override
	@Transactional
	public void delete(String brandUuid, String courseUuid) throws CourseException {
		try {
			Course entity = this.readByCourseUuid(brandUuid, courseUuid);
			
			courseRepository.delete(entity.getBrandUuid(), entity.getUuid(), requestContext.getUsername());
			
			eventPublisher.publishEvent(new CourseEvent(this, entity, OperationEnum.delete));
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, courseUuid={}", brandUuid, courseUuid, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.DELETE_FAILED, e);
		}
	}

	@Override
	@Transactional
	public void deleteAllByBrandUuid(String brandUuid) throws CourseException {
		try {
			long deletedCount = courseRepository.deleteAllByBrandUuid(brandUuid, requestContext.getUsername());
			logger.info("Course deleted for brand - brandUuid={} deletedCount={} ", brandUuid, deletedCount);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}", brandUuid, e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.DELETE_FAILED, e);
		}
	}
	
	private CourseDto updateCourse(CourseDto courseDto, boolean skipNull) throws CourseException {
		try {
			Assert.notNull(courseDto, "courseDto must not be null");
			Course course = courseMapper.toEntity(courseDto);
			
			Course oldCourse = this.readByCourseUuid(course.getBrandUuid(), course.getUuid());

			ModelMapper mapper = new ModelMapper();
			mapper.getConfiguration().setSkipNullEnabled(skipNull).setCollectionsMergeEnabled(false);

			mapper = courseMapper.initCourseMappings(mapper);
			mapper.map(course, oldCourse);

			oldCourse.setModifiedOn(Instant.now());
			oldCourse.setModifiedBy(requestContext.getUsername());

			Course saved = courseRepository.save(oldCourse);
			return courseMapper.toDto(saved);
		} catch (Exception e) {
			logger.error("Error - brandUuid={}, courseUuid={}", courseDto.getBrandUuid(), courseDto.getUuid(), e);
			if (e instanceof CourseException) {
				throw (CourseException) e;
			}
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.UPDATE_FAILED, e);
		}
	}
	
	private Course readByCourseUuid(String brandUuid, String courseUuid) throws CourseException {
        Optional<Course> entity = courseRepository.findByBrandUuidAndUuidAndIsDeletedIsFalse(brandUuid, courseUuid);
		if (entity.isEmpty()) {
			throw new CourseException(requestContext.getTrackingNumber(), CourseException.COURSE_NOT_FOUND, "Course not found");
		}
		return entity.get();
	}
}
