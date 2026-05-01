package com.iso.hypo.brand.application.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.CourseDto;
import com.iso.hypo.brand.domain.model.Course;
import com.iso.hypo.common.domain.model.LocalizedString;

@Component
public class CourseDtoMapper {

    private final ModelMapper modelMapper;

    public CourseDtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public CourseDto toDto(Course entity) {
        return map(entity, CourseDto.class);
    }

    public Course toEntity(CourseDto dto) {
        return map(dto, Course.class);
    }

    public ModelMapper initCourseMappings(ModelMapper mapper) {
		PropertyMap<Course, Course> coursePropertyMap = new PropertyMap<Course, Course>() {
			protected void configure() {
				skip().setId(null);
				skip().setUuid(null);
				skip().setCreatedOn(null);
				skip().setCreatedBy(null);
				skip().setModifiedOn(null);
				skip().setModifiedBy(null);
				skip().setDeleted(false);
				skip().setDeletedOn(null);
				skip().setDeletedBy(null);
				skip().setActive(false);
				skip().setActivatedOn(null);
				skip().setDeactivatedOn(null);
				skip().setActivatedBy(null);
				skip().setDeactivatedBy(null);
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