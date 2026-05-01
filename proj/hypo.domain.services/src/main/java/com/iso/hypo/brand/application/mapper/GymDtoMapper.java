package com.iso.hypo.brand.application.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.brand.application.dto.GymDto;
import com.iso.hypo.brand.domain.model.Coach;
import com.iso.hypo.brand.domain.model.Gym;
import com.iso.hypo.common.domain.model.contact.Contact;
import com.iso.hypo.common.domain.model.contact.PhoneNumber;
import com.iso.hypo.common.domain.model.location.Address;

@Component
public class GymDtoMapper {

    private final ModelMapper modelMapper;

    public GymDtoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public GymDto toDto(Gym entity) {
        return map(entity, GymDto.class);
    }

    public Gym toEntity(GymDto dto) {
        return map(dto, Gym.class);
    }
    
	public ModelMapper initGymMappings(ModelMapper mapper) {
		
		PropertyMap<Gym, Gym> gymPropertyMap = new PropertyMap<Gym, Gym>() {
			protected void configure() {
				skip().setId(null);
				skip().setUuid(null);
				skip().setCreatedOn(null);
				skip().setCreatedBy(null);
				skip().setModifiedOn(null);
				skip().setModifiedBy(null);
				skip().setDeleted(false);
				skip().setDeletedOn(null);
				skip().setDeletedBy(null);
				skip().setActive(false);
				skip().setActivatedOn(null);
				skip().setDeactivatedOn(null);
				skip().setActivatedBy(null);
				skip().setDeactivatedBy(null);
			}
		};
		
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
		
	    PropertyMap<Coach, Coach> coachsPropertyMap = new PropertyMap<Coach, Coach>() {
			@Override
			protected void configure() {
			}
		};
		
		mapper.addMappings(gymPropertyMap);
		mapper.addMappings(addressPropertyMap);
		mapper.addMappings(phoneNumberPropertyMap);
		mapper.addMappings(contactPropertyMap);
		mapper.addMappings(coachsPropertyMap);
		
		return mapper;
	}
}

