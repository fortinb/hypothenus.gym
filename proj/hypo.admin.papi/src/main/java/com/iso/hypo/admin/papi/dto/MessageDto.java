package com.iso.hypo.admin.papi.dto;

import com.iso.hypo.admin.papi.dto.enumeration.MessageSeverityEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {
	private String code;
	
	private String Description;
	
	private MessageSeverityEnum severity;
}
