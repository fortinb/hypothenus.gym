package com.isoceles.hypothenus.gym.domain.services;

import java.time.Instant;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.isoceles.hypothenus.gym.domain.context.RequestContext;
import com.isoceles.hypothenus.gym.domain.exception.DomainException;
import com.isoceles.hypothenus.gym.domain.model.LocalizedString;
import com.isoceles.hypothenus.gym.domain.model.aggregate.Course;
import com.isoceles.hypothenus.gym.domain.repository.CourseRepository;

@Service
public class CourseService {

	private CourseRepository courseRepository;

	@Autowired
	private RequestContext requestContext;
	
	public CourseService(CourseRepository courseRepository) {
		this.courseRepository = courseRepository;
	}

	public Course create(String brandId, String gymId, Course course) throws DomainException {
		if (!course.getBrandId().equals(brandId)) {
			throw new DomainException(DomainException.INVALID_BRAND, "Invalid brand");
		}
		
		if (!course.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		Optional<Course> existingCourse = courseRepository.findByBrandIdAndGymIdAndCodeAndIsDeletedIsFalse(brandId, gymId, course.getCode());
		if (existingCourse.isPresent()) {
			throw new DomainException(DomainException.COURSE_CODE_ALREADY_EXIST, "Duplicate course code");
		}
		
		course.setCreatedOn(Instant.now());
		course.setCreatedBy(requestContext.getUsername());
		
		return courseRepository.save(course);
	}

	public Course update(String brandId, String gymId, Course course) throws DomainException {
		if (!course.getBrandId().equals(brandId)) {
			throw new DomainException(DomainException.INVALID_BRAND, "Invalid brand");
		}
		
		if (!course.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
				
		Course oldCourse = this.findByCourseId(brandId, gymId, course.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initCourseMappings(mapper);
		mapper.map(course, oldCourse);

		oldCourse.setModifiedOn(Instant.now());
		oldCourse.setModifiedBy(requestContext.getUsername());
		
		return courseRepository.save(oldCourse);
	}

	public Course patch(String brandId, String gymId, Course course) throws DomainException {
		if (!course.getBrandId().equals(brandId)) {
			throw new DomainException(DomainException.INVALID_BRAND, "Invalid brand");
		}
		
		if (!course.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		Course oldCourse = this.findByCourseId(brandId, gymId, course.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initCourseMappings(mapper);
		mapper.map(course, oldCourse);
		
		oldCourse.setModifiedOn(Instant.now());
		oldCourse.setModifiedBy(requestContext.getUsername());
		
		return courseRepository.save(oldCourse);
	}

	public void delete(String brandId, String gymId, String courseId) throws DomainException {
		Course oldCourse = this.findByCourseId(brandId, gymId,  courseId);
		oldCourse.setDeleted(true);

		oldCourse.setDeletedOn(Instant.now());
		oldCourse.setDeletedBy(requestContext.getUsername());
		
		courseRepository.save(oldCourse);
	}

	public Course findByCourseId(String brandId, String gymId, String id) throws DomainException {
		Optional<Course> entity = courseRepository.findByBrandIdAndGymIdAndIdAndIsDeletedIsFalse(brandId, gymId, id);
		if (entity.isEmpty()) {
			throw new DomainException(DomainException.COURSE_NOT_FOUND, "Course not found");
		}

		return entity.get();
	}

	public Page<Course> list(String brandId, String gymId, int page, int pageSize, boolean includeInactive) throws DomainException {
		if (includeInactive) {
			return courseRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalse(brandId, gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
		}

		return courseRepository.findAllByBrandIdAndGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(brandId, gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
	}
	
	public Course activate(String brandId, String gymId, String id) throws DomainException {
		
		Optional<Course> oldCourse = courseRepository.activate(brandId, gymId, id);
		if (oldCourse.isEmpty()) {
			throw new DomainException(DomainException.COURSE_NOT_FOUND, "Course not found");
		}
		
		return oldCourse.get();
	}
	
	public Course deactivate(String brandId, String gymId, String id) throws DomainException {
		
		Optional<Course> oldCourse = courseRepository.deactivate(brandId, gymId, id);
		if (oldCourse.isEmpty()) {
			throw new DomainException(DomainException.COURSE_NOT_FOUND, "Course not found");
		}
		
		return oldCourse.get();
	}
	
	private ModelMapper initCourseMappings(ModelMapper mapper) {
		PropertyMap<Course, Course> coursePropertyMap = new PropertyMap<Course, Course>()
	    {
	        protected void configure()
	        {
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

		mapper.addMappings(coursePropertyMap);
		mapper.addMappings(localizedStringPropertyMap);
		
		return mapper;
	}
}
