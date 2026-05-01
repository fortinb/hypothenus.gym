package com.iso.hypo.brand.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.brand.infrastructure.persistence.entity.BrandDocument;

@Component
public class BrandDocumentMapper {

    private final ModelMapper modelMapper;

    public BrandDocumentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public BrandDocument toDocument(Brand entity) {
        return map(entity, BrandDocument.class);
    }

    public Brand toEntity(BrandDocument document) {
        return map(document, Brand.class);
    }
}

