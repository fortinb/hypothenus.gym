package com.iso.hypo.brand.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.domain.model.Coach;
import com.iso.hypo.brand.infrastructure.persistence.entity.CoachDocument;

@Component
public class CoachDocumentMapper {

    private final ModelMapper modelMapper;

    public CoachDocumentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public CoachDocument toDocument(Coach entity) {
        return map(entity, CoachDocument.class);
    }

    public Coach toEntity(CoachDocument document) {
        return map(document, Coach.class);
    }
}

