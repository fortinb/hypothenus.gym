package com.iso.hypo.brand.domain.model;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.common.domain.model.contact.Person;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Document ("coach")
public class Coach extends BaseEntity {
	
	@Id
	private String id;
	
	@Indexed
	@NonNull
	private String brandUuid;
	
	@Indexed
	private String uuid;
	
	private Person person;
	
	public Coach() {
		super();
	}
	
	public Coach(String brandUuid,
			     Person person, 
			     boolean active, 
			     Instant activatedOn, 
			     Instant deactivatedOn) {
		super(active);
		this.brandUuid = brandUuid;
		this.person = person;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
