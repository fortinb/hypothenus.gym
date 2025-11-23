package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.isoceles.hypothenus.gym.domain.model.contact.Person;
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
	@NonNull
	private String brandId;

	private Person person;
	
	public Member() {
		
	}

	public Member(String brandId,  Person person, boolean isActive, Instant startedOn, Instant endedOn) {
		super(isActive);
		this.brandId = brandId;
		this.person = person;
		this.activatedOn = startedOn;
		this.deactivatedOn = endedOn;
	}
}
