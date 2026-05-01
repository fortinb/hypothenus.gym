package com.iso.hypo.finance.application.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.finance.application.dto.FinancialInstrumentDto;
import com.iso.hypo.finance.domain.model.BankAccount;
import com.iso.hypo.finance.domain.model.CreditCard;
import com.iso.hypo.finance.domain.model.FinancialInstrument;

@Component
public class FinancialInstrumentDtoMapper {

    private final ModelMapper modelMapper;

    public FinancialInstrumentDtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public FinancialInstrumentDto toDto(FinancialInstrument entity) {
        return map(entity, FinancialInstrumentDto.class);
    }

    public FinancialInstrument toEntity(FinancialInstrumentDto dto) {
        return map(dto, FinancialInstrument.class);
    }
    
    public ModelMapper initFinancialInstrumentMappings(ModelMapper mapper) {
        PropertyMap<FinancialInstrument, FinancialInstrument> financialInstrumentPropertyMap = new PropertyMap<FinancialInstrument, FinancialInstrument>() {
            protected void configure() {
            	
            }
        };
		
		PropertyMap<CreditCard, CreditCard> creditCardPropertyMap = new PropertyMap<CreditCard, CreditCard>() {
			@Override
			protected void configure() {
				skip().setCardNumber(null);
				skip().setCardHolderName(null);
			}
		};
		
		PropertyMap<BankAccount, BankAccount> bankAccountPropertyMap = new PropertyMap<BankAccount, BankAccount>() {
			@Override
			protected void configure() {
			}
		};

        mapper.addMappings(financialInstrumentPropertyMap);
        mapper.addMappings(creditCardPropertyMap);
        mapper.addMappings(bankAccountPropertyMap);
        return mapper;
    }
}