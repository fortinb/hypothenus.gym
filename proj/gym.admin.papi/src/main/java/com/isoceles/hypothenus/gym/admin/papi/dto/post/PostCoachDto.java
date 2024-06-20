package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import java.time.Instant;
import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumberDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCoachDto {
	
	@NotBlank
	private String gymId;
	
	@NotBlank
	private String firstname;
	
	@NotBlank
	private String lastname;
	
	@NotBlank
	private String email;
	
	private String language;
	
	private boolean isActive;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private Instant startedOn;
	
	private Instant endedOn;
}
