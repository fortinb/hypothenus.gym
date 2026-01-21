package com.iso.hypo.model.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.model.aggregate.Gym;
import com.iso.hypo.model.dto.GymDto;
import com.iso.hypo.model.aggregate.Course;
import com.iso.hypo.model.dto.CourseDto;
import com.iso.hypo.model.aggregate.Coach;
import com.iso.hypo.model.dto.CoachDto;

@Component
public class GymMapper {

    private final ModelMapper modelMapper;

    public GymMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public GymDto toDto(Gym entity) {
        return map(entity, GymDto.class);
    }

    public Gym toEntity(GymDto dto) {
        return map(dto, Gym.class);
    }

    public CourseDto toDto(Course entity) {
        return map(entity, CourseDto.class);
    }

    public Course toEntity(CourseDto dto) {
        return map(dto, Course.class);
    }

    public CoachDto toDto(Coach entity) {
        return map(entity, CoachDto.class);
    }

    public Coach toEntity(CoachDto dto) {
        return map(dto, Coach.class);
    }
}
