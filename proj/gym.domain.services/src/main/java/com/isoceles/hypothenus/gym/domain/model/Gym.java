package com.isoceles.hypothenus.gym.domain.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document ("gym")
public class Gym {
	@Id
	private String id;
	
	@TextIndexed
	private String name;
	
	private Address address;
	
	@TextIndexed
	private String email;
	
	private String locale;
	
	private List<PhoneNumber> phoneNumbers;
	
	private List<SocialMediaAccount> socialMediaAccount;
}
