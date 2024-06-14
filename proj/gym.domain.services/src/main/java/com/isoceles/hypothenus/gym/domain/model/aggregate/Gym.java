package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.isoceles.hypothenus.gym.domain.model.Address;
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
	
	private String locale;
	
	private List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();
	
	private List<SocialMediaAccount> socialMediaAccounts = new ArrayList<SocialMediaAccount>();

	public Gym() {
	}
	
	public Gym(String gymId, String name, Address address, String email, String locale,
			List<PhoneNumber> phoneNumbers, List<SocialMediaAccount> socialMediaAccounts) {
		super();
		this.gymId = gymId;
		this.name = name;
		this.address = address;
		this.email = email;
		this.locale = locale;
		this.phoneNumbers = phoneNumbers;
		this.socialMediaAccounts = socialMediaAccounts;
	}


}
