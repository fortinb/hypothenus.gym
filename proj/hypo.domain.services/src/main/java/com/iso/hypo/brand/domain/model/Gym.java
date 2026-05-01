package com.iso.hypo.brand.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.iso.hypo.common.domain.model.BaseEntity;
import com.iso.hypo.common.domain.model.contact.Contact;
import com.iso.hypo.common.domain.model.contact.PhoneNumber;
import com.iso.hypo.common.domain.model.location.Address;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gym extends BaseEntity {
	
	private String id;

	private String brandUuid;

	private String uuid;
	
	private String code;

	private String name;
	
	private Address address;
	
	private String email;
	
	private String logoUri;
	
	private String note;
	
	private List<Contact> contacts;
	
	private List<PhoneNumber> phoneNumbers;
	
	private List<Coach> coachs = new ArrayList<>();
	
	public Gym() {
		super();
	}
	
	public Gym(String brandUuid, String gymUuid, String name, Address address, String email, String logoUri, boolean active,
			List<PhoneNumber> phoneNumbers, List<Contact> contacts, List<Coach> coachs, Instant activatedOn, Instant deactivatedOn) {
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
