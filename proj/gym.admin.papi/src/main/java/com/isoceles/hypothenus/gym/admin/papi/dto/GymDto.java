package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GymDto extends BaseDto {
	
	private String gymId;
	
	private String name;
	
	private AddressDto address;
	
	private String email;
	
	private String locale;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<SocialMediaAccountDto> socialMediaAccounts;
}
