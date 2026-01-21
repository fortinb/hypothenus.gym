package com.iso.hypo.model.dto;

import com.iso.hypo.model.domain.contact.Person;
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