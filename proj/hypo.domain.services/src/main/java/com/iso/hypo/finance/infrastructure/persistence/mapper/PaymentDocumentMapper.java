package com.iso.hypo.finance.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.finance.domain.model.Payment;
import com.iso.hypo.finance.infrastructure.persistence.entity.PaymentDocument;

@Component
public class PaymentDocumentMapper {

    private final ModelMapper modelMapper;

    public PaymentDocumentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public PaymentDocument toDocument(Payment entity) {
        return map(entity, PaymentDocument.class);
    }

    public Payment toEntity(PaymentDocument document) {
        return map(document, Payment.class);
    }
}

