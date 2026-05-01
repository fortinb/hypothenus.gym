package com.iso.hypo.membership.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.membership.domain.model.Member;
import com.iso.hypo.membership.infrastructure.persistence.entity.MemberDocument;


@Component
public class MemberDocumentMapper {

    private final ModelMapper modelMapper;

    public MemberDocumentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public MemberDocument toDocument(Member entity) {
        return map(entity, MemberDocument.class);
    }

    public Member toEntity(MemberDocument document) {
        return map(document, Member.class);
    }
}

