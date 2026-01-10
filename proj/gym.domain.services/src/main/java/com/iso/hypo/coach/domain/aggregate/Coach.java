package com.iso.hypo.coach.domain.aggregate;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.BaseEntity;
import com.iso.hypo.common.domain.contact.Person;
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
	private String brandId;
	
	@Indexed
	@NonNull
	private String gymId;
	
	private Person person;
	
	public Coach() {
	}
	
	public Coach(String brandId,
				 String gymId, 
			     Person person, 
			     boolean isActive, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.brandId = brandId;
		this.gymId = gymId;
		this.person = person;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
