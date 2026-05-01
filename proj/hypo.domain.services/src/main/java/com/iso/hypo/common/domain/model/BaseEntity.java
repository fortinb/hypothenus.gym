package com.iso.hypo.common.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
	
	protected boolean deleted = false;
	protected boolean active = true;
	
	@CreatedBy
	protected String createdBy;
	protected Instant createdOn;
	
	protected String deletedBy;
	protected Instant deletedOn;
	
	@LastModifiedBy
	protected String modifiedBy;
	protected Instant modifiedOn;
	
	protected Instant activatedOn;
	protected String activatedBy;
	protected Instant deactivatedOn;
	protected String deactivatedBy;
	
	public BaseEntity() {
		this.messages = new java.util.ArrayList<Message>();
	}
	
	public BaseEntity(boolean active) {
		this.active = active;
	}
	
	public void activate(String activatedBy) {
		this.active = true;
		this.activatedOn = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		this.activatedBy = activatedBy;
	}
	
	public void deactivate(String deactivatedBy) {
		this.active = false;
		this.deactivatedOn = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		this.deactivatedBy = deactivatedBy;
	}
	
	public void delete(String deletedBy) {
		this.deleted = true;
		this.deletedOn = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		this.deletedBy = deletedBy;
	}
}
