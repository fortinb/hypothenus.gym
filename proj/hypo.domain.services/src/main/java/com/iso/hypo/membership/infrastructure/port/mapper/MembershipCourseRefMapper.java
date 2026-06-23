package com.iso.hypo.membership.infrastructure.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.CourseDto;
import com.iso.hypo.membership.application.port.dto.CourseRef;

@Component
public class MembershipCourseRefMapper {

    private final ModelMapper modelMapper;

    public MembershipCourseRefMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public CourseRef toRef (CourseDto entity) {
        return map(entity, CourseRef.class);
    }

    public CourseDto toDto(CourseRef ref) {
        return map(ref, CourseDto.class);
    }
}