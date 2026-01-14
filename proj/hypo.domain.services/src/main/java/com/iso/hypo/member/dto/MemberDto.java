package com.iso.hypo.member.dto;

import com.iso.hypo.common.domain.contact.Person;
import com.iso.hypo.gym.dto.GymDto;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto extends BaseEntityDto {

    private String uuid;

    private String brandId;

    private Person person;

    private GymDto preferredGym;

}