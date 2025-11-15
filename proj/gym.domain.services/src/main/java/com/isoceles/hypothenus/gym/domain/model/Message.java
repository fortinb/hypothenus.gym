package com.isoceles.hypothenus.gym.domain.model;

import com.isoceles.hypothenus.gym.domain.model.enumeration.MessageSeverityEnum;

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
