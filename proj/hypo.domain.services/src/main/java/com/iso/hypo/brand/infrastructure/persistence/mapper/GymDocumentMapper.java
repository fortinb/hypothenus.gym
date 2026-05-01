package com.iso.hypo.brand.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.domain.model.Gym;
import com.iso.hypo.brand.infrastructure.persistence.entity.GymDocument;

@Component
public class GymDocumentMapper {

    private final ModelMapper modelMapper;

    public GymDocumentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public GymDocument toDocument(Gym entity) {
        return map(entity, GymDocument.class);
    }

    public Gym toEntity(GymDocument document) {
        return map(document, Gym.class);
    }
}

