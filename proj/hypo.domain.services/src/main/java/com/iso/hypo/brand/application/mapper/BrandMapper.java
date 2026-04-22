package com.iso.hypo.brand.application.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.BrandDto;
import com.iso.hypo.brand.domain.model.Brand;
import com.iso.hypo.common.domain.model.contact.Contact;
import com.iso.hypo.common.domain.model.contact.PhoneNumber;
import com.iso.hypo.common.domain.model.location.Address;

@Component
public class BrandMapper {

    private final ModelMapper modelMapper;

    public BrandMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public BrandDto toDto(Brand entity) {
        return map(entity, BrandDto.class);
    }

    public Brand toEntity(BrandDto dto) {
        return map(dto, Brand.class);
    }

	public ModelMapper initBrandMappings(ModelMapper mapper) {
		PropertyMap<Address, Address> addressPropertyMap = new PropertyMap<Address, Address>() {
			@Override
			protected void configure() {
			}
		};
		
		PropertyMap<PhoneNumber, PhoneNumber> phoneNumberPropertyMap = new PropertyMap<PhoneNumber, PhoneNumber>() {
			@Override
			protected void configure() {
			}
		};
		
		PropertyMap<Contact, Contact> contactPropertyMap = new PropertyMap<Contact, Contact>() {
			@Override
			protected void configure() {
			}
		};
		
		mapper.addMappings(addressPropertyMap);
		mapper.addMappings(phoneNumberPropertyMap);
		mapper.addMappings(contactPropertyMap);
		
		return mapper;
	}
}

