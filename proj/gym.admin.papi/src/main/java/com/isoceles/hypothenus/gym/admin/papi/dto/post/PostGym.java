package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import java.util.List;
import java.util.Locale;

import com.isoceles.hypothenus.gym.admin.papi.dto.Address;
import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumber;
import com.isoceles.hypothenus.gym.admin.papi.dto.SocialMediaAccount;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostGym {
	private String name;
	private Address address;
	private String email;
	private Locale language;
	private List<PhoneNumber> phoneNumbers;
	private List<SocialMediaAccount> socialMediaAccount;
}
