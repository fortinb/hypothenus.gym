package com.iso.hypo.domain.aggregate;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.domain.BaseEntity;
import com.iso.hypo.domain.contact.Person;
import com.iso.hypo.domain.enumeration.MemberTypeEnum;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("member")
public class Member extends BaseEntity {

	@Id
	private String id;
	
	@Indexed
	private String uuid;

	@Indexed
	@NonNull
	private String brandUuid;
	
	private Person person;
	
	private MemberTypeEnum memberType;
	
	private String preferredGymUuid;
	
	@DBRef
	private User user;
	
	public Member() {
		
	}

	public Member(String brandUuid, Person person, MemberTypeEnum memberType,  boolean isActive, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.brandUuid = brandUuid;
		this.person = person;
		this.memberType = memberType;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
