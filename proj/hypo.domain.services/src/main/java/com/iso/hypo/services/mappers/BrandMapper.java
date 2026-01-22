package com.iso.hypo.services.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.domain.aggregate.Brand;
import com.iso.hypo.domain.dto.BrandDto;
import com.iso.hypo.domain.aggregate.MembershipPlan;
import com.iso.hypo.domain.dto.MembershipPlanDto;

@Component
public class BrandMapper {

    private final ModelMapper modelMapper;

    public BrandMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public BrandDto toDto(Brand entity) {
        return map(entity, BrandDto.class);
    }

    public Brand toEntity(BrandDto dto) {
        return map(dto, Brand.class);
    }

    public MembershipPlanDto toDto(MembershipPlan entity) {
        return map(entity, MembershipPlanDto.class);
    }

    public MembershipPlan toEntity(MembershipPlanDto dto) {
        return map(dto, MembershipPlan.class);
    }
}

