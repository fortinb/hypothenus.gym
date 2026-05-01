package com.iso.hypo.brand.domain.model;

import java.time.Instant;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.common.domain.model.contact.Person;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Coach extends BaseEntity {
	
	private String id;

	private String brandUuid;
	
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
