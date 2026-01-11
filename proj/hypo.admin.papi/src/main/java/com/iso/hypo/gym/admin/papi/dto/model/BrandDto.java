package com.iso.hypo.gym.admin.papi.dto.model;

import java.util.List;

import com.iso.hypo.gym.admin.papi.dto.BaseDto;
import com.iso.hypo.gym.admin.papi.dto.contact.ContactDto;
import com.iso.hypo.gym.admin.papi.dto.contact.PhoneNumberDto;
import com.iso.hypo.gym.admin.papi.dto.location.AddressDto;

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
	
	private String logoUri;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<ContactDto> contacts;
}
