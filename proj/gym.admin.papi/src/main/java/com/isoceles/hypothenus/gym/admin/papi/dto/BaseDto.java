package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseDto {

	protected List<MessageDto> messages;
	
	@JsonProperty(value="isDeleted")   
	protected boolean isDeleted;
	
	@JsonProperty(value="isActive")   
	protected boolean isActive;
	
	protected String createdBy;
	protected Instant createdOn;
	
	protected String deletedBy;
	protected Instant deletedOn;
	
	protected String modifiedBy;
	protected Instant modifiedOn;
	
	private Instant activatedOn;
	private Instant deactivatedOn;
}
