package com.iso.hypo.gym.domain.aggregate;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.iso.hypo.common.domain.BaseEntity;
import com.iso.hypo.common.domain.contact.Contact;
import com.iso.hypo.common.domain.contact.PhoneNumber;
import com.iso.hypo.common.domain.location.Address;
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
	private String uuid;
	
	@Indexed
	@NonNull
	private String brandId;
	
	@Indexed (unique = true)
	@NonNull
	private String gymId;
	
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
	
	public Gym(String brandId, String gymId, String name, Address address, String email, String logoUri, boolean isActive,
			List<PhoneNumber> phoneNumbers, List<Contact> contacts, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.brandId = brandId;
		this.gymId = gymId;
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
