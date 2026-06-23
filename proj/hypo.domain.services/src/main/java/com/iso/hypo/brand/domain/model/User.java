package com.iso.hypo.brand.domain.model;

import java.time.Instant;
import java.util.List;

import com.iso.hypo.common.application.security.RoleEnum;
import com.iso.hypo.common.domain.model.BaseEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User extends BaseEntity {

	private String id;
	
	private String uuid;
	
	private String idpId;

	private String upn;
	
	private String firstname;

	private String lastname;
	
	private String email;
	
	private List<RoleEnum> roles;
	
	public User() {
		super();
	}
	
	public User(String firstname, String lastname, String email, boolean active, Instant activatedOn, Instant deactivatedOn) {
		super(active);
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
		this.roles = new java.util.ArrayList<RoleEnum>();
	}
}
