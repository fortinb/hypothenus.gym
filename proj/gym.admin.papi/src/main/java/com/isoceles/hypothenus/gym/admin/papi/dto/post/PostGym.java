package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.Address;
import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumber;
import com.isoceles.hypothenus.gym.admin.papi.dto.SocialMediaAccount;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostGym {
	
	@NotBlank
	private String name;
	
	private Address address;
	
	@NotBlank
	private String email;
	
	private String language;
	
	private List<PhoneNumber> phoneNumbers;
	
	private List<SocialMediaAccount> socialMediaAccount;
}
