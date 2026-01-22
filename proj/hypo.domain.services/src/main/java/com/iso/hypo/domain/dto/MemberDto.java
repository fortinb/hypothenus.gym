package com.iso.hypo.domain.dto;

import com.iso.hypo.domain.contact.Person;
import com.iso.hypo.common.dto.BaseEntityDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto extends BaseEntityDto {

    private String uuid;

    private String brandUuid;

    private Person person;

    private String preferredGymUuid;

}
