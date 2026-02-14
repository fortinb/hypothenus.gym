package com.iso.hypo.domain.aggregate;

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
	
	private String firstname;

	private String lastname;
	
	private String email;
	
	public User() {
	}
}
