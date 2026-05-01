package com.iso.hypo.finance.infrastructure.persistence.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.finance.domain.model.FinancialInstrument;
import com.iso.hypo.finance.infrastructure.persistence.entity.FinancialInstrumentDocument;

@Component
public class FinancialInstrumentDocumentMapper {

    private final ModelMapper modelMapper;

    public FinancialInstrumentDocumentMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public FinancialInstrumentDocument toDocument(FinancialInstrument entity) {
        return map(entity, FinancialInstrumentDocument.class);
    }

    public FinancialInstrument toEntity(FinancialInstrumentDocument document) {
        return map(document, FinancialInstrument.class);
    }
}

