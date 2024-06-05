package com.isoceles.hypothenus.gym.admin.papi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
	private String code;
	private String Description;
	private MessageSeverityEnum severity;
}
