package com.iso.hypo.admin.papi.dto.model;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.contact.PersonDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto extends BaseDto {

    private String brandUuid;
    
    private String uuid;

    private String preferredGymUuid;
    
    private PersonDto person;
}
