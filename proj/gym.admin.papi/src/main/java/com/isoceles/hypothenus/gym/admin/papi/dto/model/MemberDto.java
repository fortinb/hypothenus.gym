package com.isoceles.hypothenus.gym.admin.papi.dto.model;

import com.isoceles.hypothenus.gym.admin.papi.dto.BaseDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.contact.PersonDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto extends BaseDto {

    private String id;

    private String brandId;

    private PersonDto person;
}
