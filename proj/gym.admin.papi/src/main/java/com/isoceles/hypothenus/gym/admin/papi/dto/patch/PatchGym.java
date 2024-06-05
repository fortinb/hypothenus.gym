package com.isoceles.hypothenus.gym.admin.papi.dto.patch;

import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.Address;
import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumber;
import com.isoceles.hypothenus.gym.admin.papi.dto.SocialMediaAccount;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchGym {
	
	@NotNull
	private String id;
	
	private String name;
	
	private Address address;
	
	private String email;
	
	private String language;
	
	private List<PhoneNumber> phoneNumbers;
	
	private List<SocialMediaAccount> socialMediaAccount;
}
