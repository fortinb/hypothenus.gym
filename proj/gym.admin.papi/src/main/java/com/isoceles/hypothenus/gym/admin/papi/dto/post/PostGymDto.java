package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.isoceles.hypothenus.gym.admin.papi.dto.AddressDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.ContactDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumberDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.SocialMediaAccountDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostGymDto {
	
	@JsonIgnore
	private String id;
	
	@NotBlank
	private String gymId;
	
	@NotBlank
	private String name;
	
	private AddressDto address;
	
	@NotBlank
	private String email;
	
	private String note;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<SocialMediaAccountDto> socialMediaAccounts;
	
	private List<ContactDto> contacts;
}
