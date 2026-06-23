package com.iso.hypo.common.application.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.common.application.dto.location.AddressDto;
import com.iso.hypo.common.domain.model.location.Address;

@Component
public class AddressDtoMapper {

	private final ModelMapper modelMapper;

	public AddressDtoMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
		this.modelMapper.getConfiguration().setSkipNullEnabled(true);
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}

	public AddressDto toDto(Address entity) {
		return map(entity, AddressDto.class);
	}

	public Address toEntity(AddressDto dto) {
		return map(dto, Address.class);
	}
}
