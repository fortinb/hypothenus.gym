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

	public Course create(String gymId, Course course) throws DomainException {
		if (!course.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		course.setCreatedOn(Instant.now());
		course.setCreatedBy(requestContext.getUsername());
		
		return courseRepository.save(course);
	}

	public Course update(String gymId, Course course) throws DomainException {
		if (!course.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		Course oldCourse = this.findByCourseId(gymId, course.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(false);
		
		mapper = initCourseMappings(mapper);
		mapper.map(course, oldCourse);

		oldCourse.setModifiedOn(Instant.now());
		oldCourse.setModifiedBy(requestContext.getUsername());
		
		return courseRepository.save(oldCourse);
	}

	public Course patch(String gymId, Course course) throws DomainException {
		if (!course.getGymId().equals(gymId)) {
			throw new DomainException(DomainException.INVALID_GYM, "Invalid gym");
		}
		
		Course oldCourse = this.findByCourseId(gymId, course.getId());

		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setSkipNullEnabled(true);
		
		mapper = initCourseMappings(mapper);
		mapper.map(course, oldCourse);
		
		oldCourse.setModifiedOn(Instant.now());
		oldCourse.setModifiedBy(requestContext.getUsername());
		
		return courseRepository.save(oldCourse);
	}

	public void delete(String gymId, String courseId) throws DomainException {
		Course oldCourse = this.findByCourseId(gymId,  courseId);
		oldCourse.setDeleted(true);

		oldCourse.setDeletedOn(Instant.now());
		oldCourse.setDeletedBy(requestContext.getUsername());
		
		courseRepository.save(oldCourse);
	}

	public Course findByCourseId(String gymId, String id) throws DomainException {
		Optional<Course> entity = courseRepository.findByGymIdAndIdAndIsDeletedIsFalse(gymId, id);
		if (entity.isEmpty()) {
			throw new DomainException(DomainException.COURSE_NOT_FOUND, "Course not found");
		}

		return entity.get();
	}

	public Page<Course> list(String gymId, int page, int pageSize) throws DomainException {
		return courseRepository.findAllByGymIdAndIsDeletedIsFalse(gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
	}
	
	public Page<Course> listActive(String gymId, int page, int pageSize) throws DomainException {
		return courseRepository.findAllByGymIdAndIsDeletedIsFalseAndIsActiveIsTrue(gymId, PageRequest.of(page, pageSize, Sort.Direction.ASC, "lastname"));
	}
	
	public Course activate(String gymId, String id) throws DomainException {
		
		Optional<Course> oldCourse = courseRepository.activate(gymId, id);
		if (oldCourse.isEmpty()) {
			throw new DomainException(DomainException.COURSE_NOT_FOUND, "Course not found");
		}
		
		return oldCourse.get();
	}
	
	public Course deactivate(String gymId, String id) throws DomainException {
		
		Optional<Course> oldCourse = courseRepository.deactivate(gymId, id);
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
