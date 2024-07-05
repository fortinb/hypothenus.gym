package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.time.Instant;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoachDto extends BaseDto {
	
	private String id;
	
	private String gymId;
	
	private String firstname;
	
	private String lastname;
	
	private String email;
	
	private String language;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private Instant activatedOn;
	
	private Instant deactivatedOn;
}
