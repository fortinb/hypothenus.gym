package com.iso.hypo.gym.services;

import java.time.Instant;
import java.util.Optional;

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
import com.iso.hypo.gym.repository.CourseRepository;

@Service
public class CourseService {

	private CourseRepository courseRepository;

	@Autowired
	private RequestContext requestContext;

	public CourseService(CourseRepository courseRepository) {
		this.courseRepository = courseRepository;
	}

	public Course create(String brandId, String gymId, Course course) throws GymException {
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

		course.setCreatedOn(Instant.now());
		course.setCreatedBy(requestContext.getUsername());

		return courseRepository.save(course);
	}

	public Course update(String brandId, String gymId, Course course) throws GymException {
		if (!course.getBrandId().equals(brandId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid brand");
		}

		if (!course.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_GYM, "Invalid gym");
		}

		Course oldCourse = this.findByCourseId(brandId, gymId, course.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration()
			.setSkipNullEnabled(false)
			.setCollectionsMergeEnabled(false);

		mapper = initCourseMappings(mapper);
		mapper.map(course, oldCourse);

		oldCourse.setModifiedOn(Instant.now());
		oldCourse.setModifiedBy(requestContext.getUsername());

		return courseRepository.save(oldCourse);
	}

	public Course patch(String brandId, String gymId, Course course) throws GymException {
		if (!course.getBrandId().equals(brandId)) {
			throw new GymException(GymException.INVALID_BRAND, "Invalid brand");
		}

		if (!course.getGymId().equals(gymId)) {
			throw new GymException(GymException.INVALID_GYM, "Invalid gym");
		}

		Course oldCourse = this.findByCourseId(brandId, gymId, course.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration()
			.setSkipNullEnabled(false)
			.setCollectionsMergeEnabled(false);

		mapper = initCourseMappings(mapper);
		mapper.map(course, oldCourse);

		oldCourse.setModifiedOn(Instant.now());
		oldCourse.setModifiedBy(requestContext.getUsername());

		return courseRepository.save(oldCourse);
	}

	public void delete(String brandId, String gymId, String courseId) throws GymException {
		Course oldCourse = this.findByCourseId(brandId, gymId, courseId);
		oldCourse.setDeleted(true);

		oldCourse.setDeletedOn(Instant.now());
		oldCourse.setDeletedBy(requestContext.getUsername());

		courseRepository.save(oldCourse);
	}

	public Course findByCourseId(String brandId, String gymId, String id) throws GymException {
		Optional<Course> entity = courseRepository.findByBrandIdAndGymIdAndIdAndIsDeletedIsFalse(brandId, gymId, id);
		if (entity.isEmpty()) {
			throw new GymException(GymException.COURSE_NOT_FOUND, "Course not found");
		}

		return entity.get();
	}

	public Page<Course> list(String brandId, String gymId, int page, int pageSize, boolean includeInactive)
			throws GymException {
		if (includeInactive) {
			return courseRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalse(brandId, gymId,
					PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		}

		return courseRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, gymId,
				PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
	}

	public Course activate(String brandId, String gymId, String id) throws GymException {

		Optional<Course> oldCourse = courseRepository.activate(brandId, gymId, id);
		if (oldCourse.isEmpty()) {
			throw new GymException(GymException.COURSE_NOT_FOUND, "Course not found");
		}

		return oldCourse.get();
	}

	public Course deactivate(String brandId, String gymId, String id) throws GymException {

		Optional<Course> oldCourse = courseRepository.deactivate(brandId, gymId, id);
		if (oldCourse.isEmpty()) {
			throw new GymException(GymException.COURSE_NOT_FOUND, "Course not found");
		}

		return oldCourse.get();
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
