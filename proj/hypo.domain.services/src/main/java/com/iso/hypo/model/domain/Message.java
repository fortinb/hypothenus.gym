package com.iso.hypo.model.domain;

import com.iso.hypo.model.domain.enumeration.MessageSeverityEnum;

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
