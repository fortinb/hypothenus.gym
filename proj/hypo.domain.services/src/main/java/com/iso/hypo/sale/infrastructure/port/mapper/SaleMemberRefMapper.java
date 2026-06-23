package com.iso.hypo.sale.infrastructure.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.membership.application.dto.MemberDto;
import com.iso.hypo.sale.application.port.dto.MemberRef;

@Component
public class SaleMemberRefMapper {

	private final ModelMapper modelMapper;

	public SaleMemberRefMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}

	public MemberDto toDto(MemberRef dto) {
		return map(dto, MemberDto.class);
	}
	
    public MemberRef toRef (MemberDto entity) {
        return map(entity, MemberRef.class);
    }
}
