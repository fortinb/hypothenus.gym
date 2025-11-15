package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.isoceles.hypothenus.gym.domain.model.contact.Contact;
import com.isoceles.hypothenus.gym.domain.model.contact.PhoneNumber;
import com.isoceles.hypothenus.gym.domain.model.location.Address;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document ("brand")
public class Brand extends BaseEntity {
	
	@Id
	private String id;
	
	@Indexed (unique = true)
	private String brandId;
	
	private String name;
	
	private Address address;
	
	private String email;
	
	private String note;
	
	private String logoUrl;
	
	private List<Contact> contacts;
	
	private List<PhoneNumber> phoneNumbers;
	
	public Brand() {
	}
	
	public Brand(String brandId, String name, Address address, String email, String logoUrl, boolean isActive,
			List<PhoneNumber> phoneNumbers, List<Contact> contacts, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.brandId = brandId;
		this.name = name;
		this.address = address;
		this.email = email;
		this.logoUrl = logoUrl;
		this.phoneNumbers = phoneNumbers;
		this.contacts = contacts;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
