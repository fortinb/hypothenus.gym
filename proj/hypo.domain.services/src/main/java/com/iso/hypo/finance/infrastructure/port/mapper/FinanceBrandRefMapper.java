package com.iso.hypo.finance.infrastructure.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.BrandDto;
import com.iso.hypo.finance.application.port.dto.BrandRef;

@Component
public class FinanceBrandRefMapper {

	private final ModelMapper modelMapper;

	public FinanceBrandRefMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}

	public BrandDto toDto(BrandRef dto) {
		return map(dto, BrandDto.class);
	}
	
    public BrandRef toRef (BrandDto entity) {
        return map(entity, BrandRef.class);
    }
}
