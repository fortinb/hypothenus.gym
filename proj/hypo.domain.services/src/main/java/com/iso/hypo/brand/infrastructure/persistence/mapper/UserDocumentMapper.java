package com.iso.hypo.brand.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.domain.model.User;
import com.iso.hypo.brand.infrastructure.persistence.entity.UserDocument;

@Component
public class UserDocumentMapper {

    private final ModelMapper modelMapper;

    public UserDocumentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public UserDocument toDocument(User entity) {
        return map(entity, UserDocument.class);
    }

    public User toEntity(UserDocument document) {
        return map(document, User.class);
    }
}

