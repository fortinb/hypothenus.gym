package com.iso.hypo.brand.application.dto;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.domain.model.contact.Person;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachDto extends BaseEntityDto {

    private String brandUuid;
    
    private String uuid;

    private Person person;
}
