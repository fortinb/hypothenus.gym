package com.iso.hypo.admin.papi.dto.patch;

import java.util.List;

import com.iso.hypo.admin.papi.dto.contact.ContactDto;
import com.iso.hypo.admin.papi.dto.contact.PhoneNumberDto;
import com.iso.hypo.admin.papi.dto.location.AddressDto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchGymDto {
	
	@NotBlank
	private String brandUuid;
	
	@NotBlank
    private String uuid;
	
	private String code;
	
	private String name;
	
	private AddressDto address;
	
	private String email;
	
	private String logoUri;
	
	private String note;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<ContactDto> contacts;
}
