package com.iso.hypo.sale.infrastructure.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.membership.application.dto.MembershipDto;
import com.iso.hypo.sale.application.port.dto.MembershipRef;

@Component
public class SaleMembershipRefMapper {

	private final ModelMapper modelMapper;

	public SaleMembershipRefMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}

	public MembershipDto toDto(MembershipRef dto) {
		return map(dto, MembershipDto.class);
	}
	
    public MembershipRef toRef (MembershipDto entity) {
        return map(entity, MembershipRef.class);
    }
}
