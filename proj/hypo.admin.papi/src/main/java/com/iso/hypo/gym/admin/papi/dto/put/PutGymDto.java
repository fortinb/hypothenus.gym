package com.iso.hypo.gym.admin.papi.dto.put;

import java.util.List;

import com.iso.hypo.gym.admin.papi.dto.contact.ContactDto;
import com.iso.hypo.gym.admin.papi.dto.contact.PhoneNumberDto;
import com.iso.hypo.gym.admin.papi.dto.location.AddressDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutGymDto {
	
	@NotBlank
    private String uuid;
	
	@NotBlank
	private String brandId;
	
	@NotBlank
	private String gymId;
	
	@NotBlank
	private String name;
	
	private AddressDto address;
	
	@NotBlank
	private String email;
	
	private String logoUri;
	
	private String note;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<ContactDto> contacts;
}
