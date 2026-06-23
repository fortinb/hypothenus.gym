package com.iso.hypo.sale.infrastructure.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.BrandDto;
import com.iso.hypo.sale.application.port.dto.BrandRef;

@Component
public class SaleBrandRefMapper {

	private final ModelMapper modelMapper;

	public SaleBrandRefMapper(ModelMapper modelMapper) {
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
