package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.isoceles.hypothenus.gym.domain.model.Address;
import com.isoceles.hypothenus.gym.domain.model.Contact;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumber;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document ("gym")
public class Gym extends BaseEntity {
	
	@Id
	private String id;
	
	@Indexed
	private String brandId;
	
	@Indexed (unique = true)
	private String gymId;
	
	private String name;
	
	private Address address;
	
	private String email;
	
	private String note;
	
	private List<Contact> contacts;
	
	private List<PhoneNumber> phoneNumbers;
	
	
	
	public Gym() {
	}
	
	public Gym(String brandId, String gymId, String name, Address address, String email, boolean isActive,
			List<PhoneNumber> phoneNumbers, List<Contact> contacts, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.brandId = brandId;
		this.gymId = gymId;
		this.name = name;
		this.address = address;
		this.email = email;
		this.phoneNumbers = phoneNumbers;
		this.contacts = contacts;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
