package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.time.Instant;
import java.util.List;
import org.springframework.data.annotation.Transient;

import com.isoceles.hypothenus.gym.domain.model.Message;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseEntity {
	
	@Transient
	protected List<Message> messages;
	
	protected boolean isDeleted = false;
	
	private String createdBy;
	private Instant createdOn;
	
	private String deletedBy;
	private Instant deletedOn;
	
	private String modifiedBy;
	private Instant modifiedOn;
}
