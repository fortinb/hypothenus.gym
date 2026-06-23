package com.iso.hypo.sale.application.port.dto;

import com.iso.hypo.common.application.dto.contact.PersonDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRef {

	private String uuid;

	private String brandUuid;
	
	private PersonDto person;
	
	private String preferredFinancialInstrumenUuid;
}
