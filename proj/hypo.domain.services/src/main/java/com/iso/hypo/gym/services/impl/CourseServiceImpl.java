package com.iso.hypo.gym.services.impl;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.gym.exception.GymException;
import com.iso.hypo.common.domain.LocalizedString;
import com.iso.hypo.gym.domain.aggregate.Coach;
import com.iso.hypo.gym.domain.aggregate.Course;
import com.iso.hypo.gym.dto.CourseDto;
import com.iso.hypo.gym.repository.CoachRepository;
import com.iso.hypo.gym.repository.CourseRepository;
import com.iso.hypo.gym.mappers.GymMapper;
import com.iso.hypo.gym.services.CourseService;

@Service
public class CourseServiceImpl implements CourseService {

	private CourseRepository courseRepository;

	private CoachRepository coachRepository;

	private GymMapper gymMapper;

	@Autowired
	private RequestContext requestContext;

	public CourseServiceImpl(CoachRepository coachRepository, CourseRepository courseRepository, GymMapper gymMapper) {
		this.coachRepository = coachRepository;
		this.courseRepository = courseRepository;
		this.gymMapper = gymMapper;
	}

	@Override
	public CourseDto create(String brandId, String gymId, CourseDto courseDto) throws GymException {
		Course course = gymMapper.toEntity(courseDto);
		if (!course.getBrandId().equals(brandId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid brand");
		}

		if (!course.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_GYM, "Invalid gym");
		}

		Optional<Course> existingCourse = courseRepository.findByBrandIdAndGymIdAndCodeAndIsDeletedIsFalse(brandId,
				gymId, course.getCode());
		if (existingCourse.isPresent()) {
			throw new GymException(GymException.COURSE_CODE_ALREADY_EXIST, "Duplicate course code");
		}

		// Validate coaches
		if (course.getCoachs() != null) {
			for (Coach coach : course.getCoachs()) {
				Optional<Coach> existingCoach = coachRepository.findByBrandIdAndGymIdAndUuidAndIsDeletedIsFalse(brandId,
						gymId, coach.getUuid());
				if (existingCoach.isEmpty()) {
					throw new GymException(GymException.COACH_NOT_FOUND, "Coach not found: " + coach.getUuid());
				}
				coach.setId(existingCoach.get().getId());
			}
		}

		course.setUuid(UUID.randomUUID().toString());
		course.setCreatedOn(Instant.now());
		course.setCreatedBy(requestContext.getUsername());

		Course saved = courseRepository.save(course);
		return gymMapper.toDto(saved);
	}

	@Override
	public CourseDto update(String brandId, String gymId, CourseDto courseDto) throws GymException {
		Course course = gymMapper.toEntity(courseDto);
		if (!course.getBrandId().equals(brandId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid brand");
		}

		if (!course.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_GYM, "Invalid gym");
		}

		Course oldCourse = this.readByCourseUuid(brandId, gymId, course.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false).setCollectionsMergeEnabled(false);

		mapper = initCourseMappings(mapper);
		mapper.map(course, oldCourse);

		// Validate coaches
		if (course.getCoachs() != null) {
			for (Coach coach : oldCourse.getCoachs()) {
				Optional<Coach> existingCoach = coachRepository.findByBrandIdAndGymIdAndUuidAndIsDeletedIsFalse(brandId,
						gymId, coach.getUuid());
				if (existingCoach.isEmpty()) {
					throw new GymException(GymException.COACH_NOT_FOUND, "Coach not found: " + coach.getUuid());
				}
				coach.setId(existingCoach.get().getId());
			}
		}

		oldCourse.setModifiedOn(Instant.now());
		oldCourse.setModifiedBy(requestContext.getUsername());

		Course saved = courseRepository.save(oldCourse);
		return gymMapper.toDto(saved);
	}

	@Override
	public CourseDto patch(String brandId, String gymId, CourseDto courseDto) throws GymException {
		Course course = gymMapper.toEntity(courseDto);
		if (!course.getBrandId().equals(brandId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid brand");
		}

		if (!course.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_GYM, "Invalid gym");
		}

		Course oldCourse = this.readByCourseUuid(brandId, gymId, course.getUuid());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false).setCollectionsMergeEnabled(false);

		mapper = initCourseMappings(mapper);
		mapper.map(course, oldCourse);

		// Validate coaches
		if (course.getCoachs() != null) {
			for (Coach coach : oldCourse.getCoachs()) {
				Optional<Coach> existingCoach = coachRepository.findByBrandIdAndGymIdAndUuidAndIsDeletedIsFalse(brandId,
						gymId, coach.getUuid());
				if (existingCoach.isEmpty()) {
					throw new GymException(GymException.COACH_NOT_FOUND, "Coach not found: " + coach.getUuid());
				}
				coach.setId(existingCoach.get().getId());
			}
		}

		oldCourse.setModifiedOn(Instant.now());
		oldCourse.setModifiedBy(requestContext.getUsername());

		Course saved = courseRepository.save(oldCourse);
		return gymMapper.toDto(saved);
	}

	@Override
	public void delete(String brandId, String gymId, String courseUuid) throws GymException {
		Course entity = this.readByCourseUuid(brandId, gymId, courseUuid);
		entity.setDeleted(true);

		entity.setDeletedOn(Instant.now());
		entity.setDeletedBy(requestContext.getUsername());

		courseRepository.save(entity);
	}

	@Override
	public CourseDto findByCourseUuid(String brandId, String gymId, String courseUuid) throws GymException {
		Optional<Course> entity = courseRepository.findByBrandIdAndGymIdAndUuidAndIsDeletedIsFalse(brandId, gymId,
				courseUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.COURSE_NOT_FOUND, "Course not found");
		}

		return gymMapper.toDto(entity.get());
	}

	@Override
	public Page<CourseDto> list(String brandId, String gymId, int page, int pageSize, boolean includeInactive)
			throws GymException {
		if (includeInactive) {
			return courseRepository
					.findAllByBrandIdAndGymIdAndIsDeletedIsFalse(brandId, gymId,
							PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"))
					.map(c -> gymMapper.toDto(c));
		}

		return courseRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, gymId,
				PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname")).map(c -> gymMapper.toDto(c));
	}

	@Override
	public CourseDto activate(String brandId, String gymId, String courseUuid) throws GymException {
		Optional<Course> entity = courseRepository.activate(brandId, gymId, courseUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.COURSE_NOT_FOUND, "Course not found");
		}

		return gymMapper.toDto(entity.get());
	}

	@Override
	public CourseDto deactivate(String brandId, String gymId, String courseUuid) throws GymException {
		Optional<Course> entity = courseRepository.deactivate(brandId, gymId, courseUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.COURSE_NOT_FOUND, "Course not found");
		}

		return gymMapper.toDto(entity.get());
	}

	private Course readByCourseUuid(String brandId, String gymId, String courseUuid) throws GymException {
		Optional<Course> entity = courseRepository.findByBrandIdAndGymIdAndUuidAndIsDeletedIsFalse(brandId, gymId,
				courseUuid);
		if (entity.isEmpty()) {
			throw new GymException(GymException.COURSE_NOT_FOUND, "Course not found");
		}

		return entity.get();
	}

	private ModelMapper initCourseMappings(ModelMapper mapper) {
		PropertyMap<Course, Course> coursePropertyMap = new PropertyMap<Course, Course>() {
			protected void configure() {
				skip().setId(null);
				skip().setActive(false);
				skip().setActivatedOn(null);
				skip().setDeactivatedOn(null);
			}
		};

		PropertyMap<LocalizedString, LocalizedString> localizedStringPropertyMap = new PropertyMap<LocalizedString, LocalizedString>() {
			@Override
			protected void configure() {
			}
		};

		PropertyMap<Coach, Coach> coachPropertyMap = new PropertyMap<Coach, Coach>() {
			@Override
			protected void configure() {
			}
		};

		mapper.addMappings(coursePropertyMap);
		mapper.addMappings(localizedStringPropertyMap);
		mapper.addMappings(coachPropertyMap);

		return mapper;
	}
}
