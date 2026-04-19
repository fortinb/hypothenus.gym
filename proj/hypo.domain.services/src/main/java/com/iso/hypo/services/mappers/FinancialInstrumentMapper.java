package com.iso.hypo.services.mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.domain.aggregate.FinancialInstrument;
import com.iso.hypo.domain.dto.FinancialInstrumentDto;
import com.iso.hypo.domain.financial.BankAccount;
import com.iso.hypo.domain.financial.CreditCard;

@Component
public class FinancialInstrumentMapper {

    private final ModelMapper modelMapper;

    public FinancialInstrumentMapper(ModelMapper modelMapper) {
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