package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.isoceles.hypothenus.gym.domain.model.Person;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document ("coach")
public class Coach extends BaseEntity {
	
	@Id
	private String id;
	
	@Indexed
	private String gymId;
	
	private Person person;
	
	public Coach() {
	}
	
	public Coach(String gymId, 
			     Person person, 
			     boolean isActive, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.gymId = gymId;
		this.person = person;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
