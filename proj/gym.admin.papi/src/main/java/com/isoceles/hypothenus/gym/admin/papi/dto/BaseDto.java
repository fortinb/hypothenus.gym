package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseDto {

	protected List<MessageDto> messages;
	
	private String createdBy;
	private Instant createdOn;
	
	private String deletedBy;
	private Instant deletedOn;
	
	private String modifiedBy;
	private Instant modifiedOn;
}
