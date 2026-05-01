package com.iso.hypo.membership.domain.model;

import java.time.Instant;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.common.domain.model.contact.Person;
import com.iso.hypo.membership.domain.model.enumeration.MemberTypeEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member extends BaseEntity {

	private String id;
	
	private String uuid;

	private String brandUuid;
	
	private Person person;
	
	private MemberTypeEnum memberType;
	
	private String preferredGymUuid;
	
	private String preferredFinancialInstrumenUuid;
	
	private String userUuid;
	
	public Member() {
		super();
	}

	public Member(String brandUuid, Person person, MemberTypeEnum memberType,  boolean active, Instant activatedOn, Instant deactivatedOn) {
		super(active);
		this.brandUuid = brandUuid;
		this.person = person;
		this.memberType = memberType;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
