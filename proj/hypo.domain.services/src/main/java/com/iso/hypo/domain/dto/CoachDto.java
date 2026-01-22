package com.iso.hypo.domain.dto;

import com.iso.hypo.domain.contact.Person;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachDto extends BaseEntityDto {

    private String brandUuid;

    private String gymUuid;
    
    private String uuid;

    private Person person;
}
