package com.iso.hypo.model.aggregate;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.model.domain.BaseEntity;
import com.iso.hypo.model.domain.contact.Contact;
import com.iso.hypo.model.domain.contact.PhoneNumber;
import com.iso.hypo.model.domain.location.Address;
import com.mongodb.lang.NonNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document ("gym")
public class Gym extends BaseEntity {
	
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
	
	public Gym() {
	}
	
	public Gym(String brandUuid, String gymUuid, String name, Address address, String email, String logoUri, boolean isActive,
			List<PhoneNumber> phoneNumbers, List<Contact> contacts, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.brandUuid = brandUuid;
		this.code = gymUuid;
		this.name = name;
		this.address = address;
		this.email = email;
		this.logoUri = logoUri;
		this.phoneNumbers = phoneNumbers;
		this.contacts = contacts;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
