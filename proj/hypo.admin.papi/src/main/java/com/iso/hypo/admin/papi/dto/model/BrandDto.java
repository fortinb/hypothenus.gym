package com.iso.hypo.admin.papi.dto.model;

import java.util.List;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.contact.ContactDto;
import com.iso.hypo.admin.papi.dto.contact.PhoneNumberDto;
import com.iso.hypo.admin.papi.dto.location.AddressDto;
import com.iso.hypo.common.domain.model.finance.Currency;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandDto extends BaseDto {
	
    private String uuid;
	
	private String code;
	
	private String name;
	
	private AddressDto address;
	
	private Currency currency;
	
	private String email;
	
	private String note;
	
	private String logoUri;
	
	private List<PhoneNumberDto> phoneNumbers;
	
	private List<ContactDto> contacts;
}
