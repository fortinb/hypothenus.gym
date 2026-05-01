package com.iso.hypo.membership.infrastructure.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.UserDto;
import com.iso.hypo.membership.application.port.dto.UserRef;

@Component
public class UserRefMapper {

    private final ModelMapper modelMapper;

    public UserRefMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public UserRef toRef (UserDto entity) {
        return map(entity, UserRef.class);
    }

    public UserDto toEntity(UserRef ref) {
        return map(ref, UserDto.class);
    }
}