package com.iso.hypo.membership.infrastructure.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.GymDto;
import com.iso.hypo.membership.application.port.dto.GymRef;

@Component
public class MembershipGymRefMapper {

    private final ModelMapper modelMapper;

    public MembershipGymRefMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public GymRef toRef (GymDto entity) {
        return map(entity, GymRef.class);
    }

    public GymDto toDto(GymRef ref) {
        return map(ref, GymDto.class);
    }
}