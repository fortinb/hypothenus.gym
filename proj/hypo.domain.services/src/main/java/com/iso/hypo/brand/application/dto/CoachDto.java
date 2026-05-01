package com.iso.hypo.brand.application.dto;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.application.dto.contact.PersonDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachDto extends BaseEntityDto {

    private String brandUuid;
    
    private String uuid;

    private PersonDto person;
}
