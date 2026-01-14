package com.iso.hypo.gym.dto;

import com.iso.hypo.common.domain.contact.Person;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachDto extends BaseEntityDto {

    private String uuid;

    private String brandId;

    private String gymId;

    private Person person;

}