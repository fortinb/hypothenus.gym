package com.isoceles.hypothenus.gym.admin.papi.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandDto extends BaseDto {
	
	private String id;
	
	private String brandId;
	
	private String name;
	
	private AddressDto address;
	
	private String email;
	
	private String note;
	
	private String logoUrl;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<ContactDto> contacts;
}
