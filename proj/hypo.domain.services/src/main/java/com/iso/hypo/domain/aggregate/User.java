package com.iso.hypo.domain.aggregate;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.domain.BaseEntity;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("user")
public class User extends BaseEntity {

	@Id
	private String id;
	
	private String uuid;
	
	private String idpId;
	
	@Indexed (unique = true)
	@NonNull
	private String upn;
	
	@NonNull
	private String firstname;

	@NonNull
	private String lastname;
	
	@NonNull
	private String email;
	
	public User() {
		
	}
	
	public User(String firstname, String lastname, String email, boolean isActive, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
