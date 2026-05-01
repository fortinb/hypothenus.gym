package com.iso.hypo.brand.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.model.contact.Contact;
import com.iso.hypo.common.domain.model.contact.PhoneNumber;
import com.iso.hypo.common.domain.model.location.Address;
import com.iso.hypo.common.infrastructure.persistence.entity.BaseDocument;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document ("gym")
public class GymDocument extends BaseDocument {
	
	@Id
	private String id;
	
	@Indexed
	@NonNull
	private String brandUuid;
	
	@Indexed
	private String uuid;
	
	@Indexed (unique = true)
	@NonNull
	private String code;
	
	@NonNull
	private String name;
	
	private Address address;
	
	private String email;
	
	private String logoUri;
	
	private String note;
	
	private List<Contact> contacts;
	
	private List<PhoneNumber> phoneNumbers;
	
	@DBRef
	private List<CoachDocument> coachs = new ArrayList<>();
	
	public GymDocument() {
		super();
	}
	
	public GymDocument(String brandUuid, String gymUuid, String name, Address address, String email, String logoUri, boolean active,
			List<PhoneNumber> phoneNumbers, List<Contact> contacts, List<CoachDocument> coachs, Instant activatedOn, Instant deactivatedOn) {
		super(active);
		this.brandUuid = brandUuid;
		this.code = gymUuid;
		this.name = name;
		this.address = address;
		this.email = email;
		this.logoUri = logoUri;
		this.phoneNumbers = phoneNumbers;
		this.contacts = contacts;
		this.coachs = coachs != null ? coachs : new ArrayList<>();
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
