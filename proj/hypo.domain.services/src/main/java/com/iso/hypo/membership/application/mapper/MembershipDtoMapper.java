package com.iso.hypo.membership.application.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.stereotype.Component;

import com.iso.hypo.membership.application.dto.MemberDto;
import com.iso.hypo.membership.application.dto.MembershipDto;
import com.iso.hypo.membership.domain.model.Member;
import com.iso.hypo.membership.domain.model.Membership;

@Component
public class MembershipDtoMapper {

    private final ModelMapper modelMapper;

    public MembershipDtoMapper(ModelMapper modelMapper) {
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

    public MembershipDto toDto(Membership entity) {
        return map(entity, MembershipDto.class);
    }

    public Membership toEntity(MembershipDto dto) {
        return map(dto, Membership.class);
    }
    
	public ModelMapper initMembershipMappings(ModelMapper mapper) {
		PropertyMap<Membership, Membership> membershipPropertyMap = new PropertyMap<Membership, Membership>()
	    {
	        protected void configure()
	        {
	            // Do not allow these fields to be overwritten by incoming DTOs
	            skip().setId(null);
	            skip().setActive(false);
	            skip().setActivatedOn(null);
	            skip().setDeactivatedOn(null);
	            // Don't replace DBRefs (references) during mapping; updates should manage references explicitly
	           // skip().setMember(null);
	        }
	    };

		mapper.addMappings(membershipPropertyMap);
		return mapper;
	}
}

