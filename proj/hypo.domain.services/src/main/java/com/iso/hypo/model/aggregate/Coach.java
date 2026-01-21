package com.iso.hypo.model.aggregate;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.model.domain.BaseEntity;
import com.iso.hypo.model.domain.contact.Person;
import com.mongodb.lang.NonNull;

import lombok.Getter;
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
	@NonNull
	private String gymUuid;
	
	@Indexed
	private String uuid;
	
	private Person person;
	
	public Coach() {
	}
	
	public Coach(String brandUuid,
				 String gymUuid, 
			     Person person, 
			     boolean isActive, 
			     Instant activatedOn, 
			     Instant deactivatedOn) {
		super(isActive);
		this.brandUuid = brandUuid;
		this.gymUuid = gymUuid;
		this.person = person;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
