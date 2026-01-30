package com.iso.hypo.domain.dto.search;

import com.iso.hypo.domain.contact.Person;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberSearchDto {

    private String brandUuid;

    private String uuid;

    private Person person;

    private String preferredGymUuid;

    private boolean isActive;
}