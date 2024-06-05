package com.isoceles.hypothenus.gym.admin.papi.dto.put;

import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.Address;
import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumber;
import com.isoceles.hypothenus.gym.admin.papi.dto.SocialMediaAccount;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutGym {
	@NotNull
	private String id;
	
	@NotBlank
	private String name;
	
	private Address address;
	
	@NotBlank
	private String email;
	
	private String language;
	
	private List<PhoneNumber> phoneNumbers;
	
	private List<SocialMediaAccount> socialMediaAccount;
}
