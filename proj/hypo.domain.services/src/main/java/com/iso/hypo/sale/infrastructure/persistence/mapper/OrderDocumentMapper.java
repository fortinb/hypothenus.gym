package com.iso.hypo.sale.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.sale.domain.model.Order;
import com.iso.hypo.sale.infrastructure.persistence.entity.OrderDocument;

@Component
public class OrderDocumentMapper {

	private final ModelMapper modelMapper;

	public OrderDocumentMapper(ModelMapper modelMapper) {
		this.modelMapper = modelMapper;
	}

	public <D> D map(Object source, Class<D> destinationType) {
		if (source == null) return null;
		return modelMapper.map(source, destinationType);
	}

	public OrderDocument toDocument(Order entity) {
		return map(entity, OrderDocument.class);
	}

	public Order toEntity(OrderDocument document) {
		return map(document, Order.class);
	}
}
