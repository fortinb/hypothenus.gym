package com.isoceles.hypothenus.gym.admin.papi.dto.patch;

import java.util.List;

import com.isoceles.hypothenus.gym.admin.papi.dto.AddressDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.ContactDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumberDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchGymDto {
	
	@NotBlank
	private String id;
	
	@NotBlank
	private String brandId;
	
	@NotBlank
	private String gymId;
	
	private String name;
	
	private AddressDto address;
	
	private String email;
	
	private String note;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<ContactDto> contacts;
}
