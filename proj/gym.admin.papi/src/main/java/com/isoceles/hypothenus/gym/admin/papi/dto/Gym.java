package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Gym {
	private String id;
	
	private String name;
	
	private Address address;
	
	private String email;
	
	private String language;
	
	private List<PhoneNumber> phoneNumbers;
	
	private List<SocialMediaAccount> socialMediaAccount;
}
