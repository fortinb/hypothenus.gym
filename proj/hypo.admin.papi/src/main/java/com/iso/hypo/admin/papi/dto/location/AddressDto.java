package com.iso.hypo.admin.papi.dto.location;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {

	@NotBlank
	private String civicNumber;
	
	@NotBlank
	private String streetName;
	
	private String appartment;
	
	@NotBlank
	private String city;
	
	@NotBlank
	private String state;
	
	@NotBlank
	private String zipCode;
	
}
