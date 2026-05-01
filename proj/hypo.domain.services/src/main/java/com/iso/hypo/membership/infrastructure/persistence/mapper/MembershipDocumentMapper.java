package com.iso.hypo.membership.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.membership.domain.model.Membership;
import com.iso.hypo.membership.infrastructure.persistence.entity.MembershipDocument;


@Component
public class MembershipDocumentMapper {

    private final ModelMapper modelMapper;

    public MembershipDocumentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public MembershipDocument toDocument(Membership entity) {
        return map(entity, MembershipDocument.class);
    }

    public Membership toEntity(MembershipDocument document) {
        return map(document, Membership.class);
    }
}

