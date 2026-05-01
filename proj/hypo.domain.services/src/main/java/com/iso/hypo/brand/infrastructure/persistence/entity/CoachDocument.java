package com.iso.hypo.brand.infrastructure.persistence.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.model.contact.Person;
import com.iso.hypo.common.infrastructure.persistence.entity.BaseDocument;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@Document ("coach")
public class CoachDocument extends BaseDocument {
	
	@Id
	private String id;
	
	@Indexed
	@NonNull
	private String brandUuid;
	
	@Indexed
	private String uuid;
	
	private Person person;
	
	public CoachDocument() {
		super();
	}
	
	public CoachDocument(String brandUuid,
			     Person person, 
			     boolean active, 
			     Instant activatedOn, 
			     Instant deactivatedOn) {
		super(active);
		this.brandUuid = brandUuid;
		this.person = person;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
