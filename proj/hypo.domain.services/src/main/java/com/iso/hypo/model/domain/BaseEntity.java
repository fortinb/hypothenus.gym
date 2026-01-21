package com.iso.hypo.model.domain;

import java.time.Instant;

import java.util.List;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseEntity {
	
	@Transient
	protected List<Message> messages;
	
	protected boolean isDeleted = false;
	protected boolean isActive = true;
	
	@CreatedBy
	protected String createdBy;
	protected Instant createdOn;
	
	protected String deletedBy;
	protected Instant deletedOn;
	
	@LastModifiedBy
	protected String modifiedBy;
	protected Instant modifiedOn;
	
	protected Instant activatedOn;
	protected Instant deactivatedOn;
	
	public BaseEntity() {
	}
	
	public BaseEntity(boolean isActive) {
		this.isActive = isActive;
	}
}
