package com.iso.hypo.brand.application.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.UserDto;
import com.iso.hypo.brand.domain.model.User;

@Component
public class UserMapper {

    private final ModelMapper modelMapper;

    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public UserDto toDto(User entity) {
        return map(entity, UserDto.class);
    }

    public User toEntity(UserDto dto) {
        return map(dto, User.class);
    }
    
    public ModelMapper initUserMappings(ModelMapper mapper) {
        PropertyMap<User, User> userPropertyMap = new PropertyMap<User, User>() {
            protected void configure() {
                skip().setId(null);
                skip().setActive(false);
                skip().setActivatedOn(null);
                skip().setDeactivatedOn(null);
            }
        };

        mapper.addMappings(userPropertyMap);
        return mapper;
    }
}