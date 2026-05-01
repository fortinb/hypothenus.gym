package com.iso.hypo.brand.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.model.enumeration.RoleEnum;
import com.iso.hypo.common.infrastructure.persistence.entity.BaseDocument;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("user")
public class UserDocument extends BaseDocument {

	@Id
	private String id;
	
	private String uuid;
	
	@Indexed (unique = true)
	private String idpId;
	
	private String upn;
	
	@NonNull
	private String firstname;

	@NonNull
	private String lastname;
	
	@NonNull
	private String email;
	
	private List<RoleEnum> roles;
	
	public UserDocument() {
		super();
	}
	
	public UserDocument(String firstname, String lastname, String email, boolean active, Instant activatedOn, Instant deactivatedOn) {
		super(active);
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
		this.roles = new java.util.ArrayList<RoleEnum>();
	}
}
