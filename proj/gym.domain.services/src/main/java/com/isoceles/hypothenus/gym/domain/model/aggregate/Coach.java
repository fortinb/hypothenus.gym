package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.isoceles.hypothenus.gym.domain.model.PhoneNumber;

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
	
	private String firstname;
	
	private String lastname;
	
	private String email;
	
	private String language;
	
	private boolean isActive;
	
	private List<PhoneNumber> phoneNumbers;
	
	private Instant activatedOn;
	
	private Instant deactivatedOn;
	
	public Coach() {
	}
	
	public Coach(String gymId, String firstname, String lastname, String email, String language,
			List<PhoneNumber> phoneNumbers, boolean isActive, Instant activatedOn, Instant deactivatedOn) {
		super();
		this.gymId = gymId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.language = language;
		this.phoneNumbers = phoneNumbers;
		this.isActive = isActive;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
