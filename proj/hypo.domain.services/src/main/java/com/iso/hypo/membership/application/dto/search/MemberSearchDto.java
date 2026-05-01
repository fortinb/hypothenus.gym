package com.iso.hypo.membership.application.dto.search;

import com.iso.hypo.common.application.dto.contact.PersonDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSearchDto {

    private String brandUuid;

    private String uuid;

	private PersonDto person;
    
    private boolean active;
}