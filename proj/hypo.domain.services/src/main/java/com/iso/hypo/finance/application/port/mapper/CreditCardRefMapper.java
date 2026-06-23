package com.iso.hypo.finance.application.port.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.finance.application.port.dto.paymentprovider.CreditCardRef;
import com.iso.hypo.finance.domain.model.CreditCard;

@Component
public class CreditCardRefMapper {

	private final ModelMapper modelMapper;

	public CreditCardRefMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}
	
	public CreditCardRef toRef(CreditCard entity) {
		return map(entity, CreditCardRef.class);
	}
}
