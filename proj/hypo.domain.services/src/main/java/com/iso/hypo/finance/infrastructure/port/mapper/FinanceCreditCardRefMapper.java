package com.iso.hypo.finance.infrastructure.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.finance.application.dto.CreditCardDto;
import com.iso.hypo.finance.application.port.dto.paymentprovider.CreditCardRef;

@Component
public class FinanceCreditCardRefMapper {

	private final ModelMapper modelMapper;

	public FinanceCreditCardRefMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}

	public CreditCardDto toDto(CreditCardRef dto) {
		return map(dto, CreditCardDto.class);
	}
	
    public CreditCardRef toRef (CreditCardDto entity) {
        return map(entity, CreditCardRef.class);
    }
}
