package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.isoceles.hypothenus.gym.domain.model.Address;
import com.isoceles.hypothenus.gym.domain.model.Contact;
import com.isoceles.hypothenus.gym.domain.model.PhoneNumber;
import com.isoceles.hypothenus.gym.domain.model.SocialMediaAccount;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document ("gym")
public class Gym extends BaseEntity {
	
	@Id
	private String id;
	
	@Indexed (unique = true)
	private String gymId;
	
	private String name;
	
	private Address address;
	
	private String email;
	
	private String note;
	
	private List<Contact> contacts;
	
	private List<PhoneNumber> phoneNumbers;
	
	private List<SocialMediaAccount> socialMediaAccounts;
	
	public Gym() {
	}
	
	public Gym(String gymId, String name, Address address, String email, boolean isActive,
			List<PhoneNumber> phoneNumbers, List<SocialMediaAccount> socialMediaAccounts, List<Contact> contacts, Instant activatedOn, Instant deactivatedOn) {
		super(isActive);
		this.gymId = gymId;
		this.name = name;
		this.address = address;
		this.email = email;
		this.phoneNumbers = phoneNumbers;
		this.socialMediaAccounts = socialMediaAccounts;
		this.contacts = contacts;
		this.activatedOn = activatedOn;
		this.deactivatedOn = deactivatedOn;
	}
}
