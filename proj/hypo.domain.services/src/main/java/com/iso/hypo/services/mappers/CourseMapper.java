package com.iso.hypo.services.mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.domain.aggregate.Gym;
import com.iso.hypo.domain.dto.GymDto;
import com.iso.hypo.domain.aggregate.Course;
import com.iso.hypo.domain.dto.CourseDto;
import com.iso.hypo.domain.LocalizedString;
import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.dto.CoachDto;

@Component
public class CourseMapper {

    private final ModelMapper modelMapper;

    public CourseMapper(ModelMapper modelMapper) {
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
    
    public ModelMapper initCourseMappings(ModelMapper mapper) {
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

