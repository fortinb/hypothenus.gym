package com.iso.hypo.sale.infrastructure.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.finance.application.dto.PaymentDto;
import com.iso.hypo.sale.application.port.dto.PaymentRef;

@Component
public class SalePaymentRefMapper {

	private final ModelMapper modelMapper;

	public SalePaymentRefMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}

	public PaymentDto toDto(PaymentRef dto) {
		return map(dto, PaymentDto.class);
	}
	
    public PaymentRef toRef (PaymentDto entity) {
        return map(entity, PaymentRef.class);
    }
}
