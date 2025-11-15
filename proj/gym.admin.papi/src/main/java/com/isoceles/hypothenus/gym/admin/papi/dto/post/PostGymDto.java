package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.contact.ContactDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.contact.PhoneNumberDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.location.AddressDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostGymDto {
	
	private String id;
	
	@NotBlank
	private String brandId;
	
	@NotBlank
	private String gymId;
	
	@NotBlank
	private String name;
	
	private AddressDto address;
	
	@NotBlank
	private String email;
	
	private String note;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<ContactDto> contacts;
}
