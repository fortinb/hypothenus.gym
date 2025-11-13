package com.isoceles.hypothenus.gym.admin.papi.dto.post;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.isoceles.hypothenus.gym.admin.papi.dto.AddressDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.ContactDto;
import com.isoceles.hypothenus.gym.admin.papi.dto.PhoneNumberDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostBrandDto {
	
	@JsonIgnore
	private String id;
	
	@NotBlank
	private String brandId;
	
	private String name;
	
	private AddressDto address;
	
	private String email;
	
	private String note;
	
	private String logoUrl;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<ContactDto> contacts;
}
