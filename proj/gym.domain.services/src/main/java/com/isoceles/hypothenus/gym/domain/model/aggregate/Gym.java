package com.isoceles.hypothenus.gym.domain.model.aggregate;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
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
	
	@TextIndexed
	private String name;
	
	private Address address;
	
	@TextIndexed
	private String email;
	
	private String locale;
	
	private List<PhoneNumber> phoneNumbers;
	
	private List<SocialMediaAccount> socialMediaAccounts;
}
