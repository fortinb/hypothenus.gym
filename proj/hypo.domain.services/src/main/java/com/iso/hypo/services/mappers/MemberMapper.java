package com.iso.hypo.services.mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.domain.aggregate.Member;
import com.iso.hypo.domain.contact.Person;
import com.iso.hypo.domain.contact.PhoneNumber;
import com.iso.hypo.domain.location.Address;
import com.iso.hypo.domain.dto.MemberDto;

@Component
public class MemberMapper {

    private final ModelMapper modelMapper;

    public MemberMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <D> D map(Object source, Class<D> destinationType) {
        if (source == null) return null;
        return modelMapper.map(source, destinationType);
    }

    public MemberDto toDto(Member entity) {
        return map(entity, MemberDto.class);
    }

    public Member toEntity(MemberDto dto) {
        return map(dto, Member.class);
    }
    
    public ModelMapper initMemberMappings(ModelMapper mapper) {
        PropertyMap<Member, Member> memberPropertyMap = new PropertyMap<Member, Member>() {
            protected void configure() {
                skip().setId(null);
                skip().setActive(false);
                skip().setActivatedOn(null);
                skip().setDeactivatedOn(null);
                skip().setUser(null);
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

        mapper.addMappings(memberPropertyMap);
        mapper.addMappings(personPropertyMap);
        mapper.addMappings(addressPropertyMap);
        mapper.addMappings(phoneNumberPropertyMap);
        return mapper;
    }
}