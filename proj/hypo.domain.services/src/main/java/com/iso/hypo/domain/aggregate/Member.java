package com.iso.hypo.domain.aggregate;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.domain.BaseEntity;
import com.iso.hypo.domain.contact.Person;
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
	private String uuid = UUID.randomUUID().toString();

	@Indexed
	@NonNull
	private String brandUuid;
	
	private Person person;
	
	private String preferredGymUuid;
	
	public Member() {
		
	}

	public Member(String brandUuid,  Person person, boolean isActive, Instant startedOn, Instant endedOn) {
		super(isActive);
		this.brandUuid = brandUuid;
		this.person = person;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
