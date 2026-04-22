package com.iso.hypo.admin.papi.dto.model;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.contact.PersonDto;
import com.iso.hypo.membership.domain.model.enumeration.MemberTypeEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDto extends BaseDto {

	private String brandUuid;
	
	private String uuid;

	private PersonDto person;
	
	private MemberTypeEnum memberType;
	
	private String preferredGymUuid;
	
	private String preferredFinancialInstrumenUuid;

}
