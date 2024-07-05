package com.isoceles.hypothenus.gym.admin.papi.dto.put;

import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.AddressDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumberDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.SocialMediaAccountDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutGymDto {
	
	@NotBlank
	private String gymId;
	
	@NotBlank
	private String name;
	
	private AddressDto address;
	
	@NotBlank
	private String email;
	
	private String language;
	
	private boolean isActive;
	
	private String note;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<SocialMediaAccountDto> socialMediaAccounts;
}
