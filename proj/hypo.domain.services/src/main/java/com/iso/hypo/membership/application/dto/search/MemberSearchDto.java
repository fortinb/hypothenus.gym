package com.iso.hypo.membership.application.dto.search;

import com.iso.hypo.common.domain.model.contact.Person;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSearchDto {

    private String brandUuid;

    private String uuid;

	private Person person;
    
    private boolean active;
}