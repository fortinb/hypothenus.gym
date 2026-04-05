package com.iso.hypo.admin.papi.dto;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseDto {

	protected List<MessageDto> messages;
	
	protected boolean deleted = false;
	protected boolean active = true;
	
	protected String createdBy;
	protected Instant createdOn;
	
	protected String deletedBy;
	protected Instant deletedOn;
	
	protected String modifiedBy;
	protected Instant modifiedOn;
	
	private Instant activatedOn;
	private Instant deactivatedOn;
}