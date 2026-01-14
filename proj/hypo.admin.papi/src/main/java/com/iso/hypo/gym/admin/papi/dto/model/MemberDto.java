package com.iso.hypo.gym.admin.papi.dto.model;

import com.iso.hypo.gym.admin.papi.dto.BaseDto;
import com.iso.hypo.gym.admin.papi.dto.contact.PersonDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto extends BaseDto {

    private String uuid;

    private String brandId;

    private PersonDto person;
}
