package com.iso.hypo.common.domain.model;

import com.iso.hypo.common.domain.model.enumeration.MessageSeverityEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
	private String code;
	
	private String Description;
	
	private MessageSeverityEnum severity;
	
	public Message() {
	}
}
