package com.iso.hypo.domain.aggregate;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.domain.BaseEntity;
import com.iso.hypo.domain.contact.Person;

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
			     boolean isActive, 
			     Instant activatedOn, 
			     Instant deactivatedOn) {
		super(isActive);
		this.brandUuid = brandUuid;
		this.person = person;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
