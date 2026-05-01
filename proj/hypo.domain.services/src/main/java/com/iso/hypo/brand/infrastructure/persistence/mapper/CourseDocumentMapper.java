package com.iso.hypo.brand.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.domain.model.Course;
import com.iso.hypo.brand.infrastructure.persistence.entity.CourseDocument;

@Component
public class CourseDocumentMapper {

    private final ModelMapper modelMapper;

    public CourseDocumentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public CourseDocument toDocument(Course entity) {
        return map(entity, CourseDocument.class);
    }

    public Course toEntity(CourseDocument document) {
        return map(document, Course.class);
    }
}

