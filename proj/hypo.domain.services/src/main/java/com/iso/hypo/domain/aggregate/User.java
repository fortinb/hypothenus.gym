package com.iso.hypo.domain.aggregate;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.domain.BaseEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("user")
public class User extends BaseEntity {

	@Id
	private String id;
	
	private String uuid; // IDP uuid
	
	@Indexed
	private String brandUuid; 
	
	private String memberdUuid;
	
	@DBRef
	private Member member;
	
	public User() {
	}
}
