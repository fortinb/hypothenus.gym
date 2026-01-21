package com.iso.hypo.model.mappers;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import com.iso.hypo.model.aggregate.Member;
import com.iso.hypo.model.dto.MemberDto;
import com.iso.hypo.model.aggregate.Membership;
import com.iso.hypo.model.dto.MembershipDto;

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

    public MembershipDto toDto(Membership entity) {
        return map(entity, MembershipDto.class);
    }

    public Membership toEntity(MembershipDto dto) {
        return map(dto, Membership.class);
    }
}
