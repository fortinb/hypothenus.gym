package com.iso.hypo.membership.infrastructure.persistence.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.brand.domain.model.User;
import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.common.domain.model.contact.Person;
import com.iso.hypo.membership.domain.model.enumeration.MemberTypeEnum;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("member")
public class MemberDocument extends BaseEntity {

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
	
	private String preferredFinancialInstrumenUuid;
	
	private String userUuid;
	
	public MemberDocument() {
		super();
	}

	public MemberDocument(String brandUuid, Person person, MemberTypeEnum memberType,  boolean active, Instant activatedOn, Instant deactivatedOn) {
		super(active);
		this.brandUuid = brandUuid;
		this.person = person;
		this.memberType = memberType;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
