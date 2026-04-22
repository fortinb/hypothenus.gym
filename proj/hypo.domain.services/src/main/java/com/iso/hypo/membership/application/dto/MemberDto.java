package com.iso.hypo.membership.application.dto;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.domain.model.contact.Person;
import com.iso.hypo.membership.domain.model.enumeration.MemberTypeEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto extends BaseEntityDto {

	private String uuid;

	private String brandUuid;
	
	private Person person;
	
	private MemberTypeEnum memberType;
	
	private String preferredGymUuid;
	
	private String preferredFinancialInstrumenUuid;
}
