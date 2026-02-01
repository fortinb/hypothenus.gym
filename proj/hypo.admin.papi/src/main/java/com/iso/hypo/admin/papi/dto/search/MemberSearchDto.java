package com.iso.hypo.admin.papi.dto.search;

import com.iso.hypo.admin.papi.dto.contact.PersonDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSearchDto {

    private String brandUuid;

    private String uuid;

	private PersonDto person;
    
    private boolean isActive;
}