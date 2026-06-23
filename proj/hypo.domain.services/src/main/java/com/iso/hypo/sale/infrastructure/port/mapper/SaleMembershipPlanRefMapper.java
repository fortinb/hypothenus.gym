package com.iso.hypo.sale.infrastructure.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.membership.application.dto.MembershipPlanDto;
import com.iso.hypo.sale.application.port.dto.MembershipPlanRef;

@Component
public class SaleMembershipPlanRefMapper {

	private final ModelMapper modelMapper;

	public SaleMembershipPlanRefMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}

	public MembershipPlanDto toDto(MembershipPlanRef dto) {
		return map(dto, MembershipPlanDto.class);
	}
	
    public MembershipPlanRef toRef (MembershipPlanDto entity) {
        return map(entity, MembershipPlanRef.class);
    }
}
