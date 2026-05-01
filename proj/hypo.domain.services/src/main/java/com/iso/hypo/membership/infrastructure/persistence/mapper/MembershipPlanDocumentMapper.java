package com.iso.hypo.membership.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.membership.domain.model.MembershipPlan;
import com.iso.hypo.membership.infrastructure.persistence.entity.MembershipPlanDocument;


@Component
public class MembershipPlanDocumentMapper {

    private final ModelMapper modelMapper;

    public MembershipPlanDocumentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public MembershipPlanDocument toDocument(MembershipPlan entity) {
        return map(entity, MembershipPlanDocument.class);
    }

    public MembershipPlan toEntity(MembershipPlanDocument document) {
        return map(document, MembershipPlan.class);
    }
}

