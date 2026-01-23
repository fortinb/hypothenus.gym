package com.iso.hypo.services.mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.domain.aggregate.Coach;
import com.iso.hypo.domain.contact.Contact;
import com.iso.hypo.domain.contact.Person;
import com.iso.hypo.domain.contact.PhoneNumber;
import com.iso.hypo.domain.dto.CoachDto;
import com.iso.hypo.domain.location.Address;

@Component
public class CoachMapper {

    private final ModelMapper modelMapper;

    public CoachMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public CoachDto toDto(Coach entity) {
        return map(entity, CoachDto.class);
    }

    public Coach toEntity(CoachDto dto) {
        return map(dto, Coach.class);
    }
    
    public ModelMapper initCoachMappings(ModelMapper mapper) {
		PropertyMap<Coach, Coach> coachPropertyMap = new PropertyMap<Coach, Coach>()
	    {
	        protected void configure()
	        {
	            skip().setId(null);
	            skip().setActive(false);
	            skip().setActivatedOn(null);
	            skip().setDeactivatedOn(null);
	        }
	    };
		
	    PropertyMap<Person, Person> personPropertyMap = new PropertyMap<Person, Person>() {
			@Override
			protected void configure() {

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
		mapper.addMappings(coachPropertyMap);
		mapper.addMappings(personPropertyMap);
		mapper.addMappings(addressPropertyMap);
		mapper.addMappings(phoneNumberPropertyMap);
		mapper.addMappings(contactPropertyMap);
		
		return mapper;
	}
}

