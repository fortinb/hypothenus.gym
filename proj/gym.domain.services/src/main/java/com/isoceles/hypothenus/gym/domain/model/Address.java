package com.isoceles.hypothenus.gym.domain.model;

import org.springframework.data.mongodb.core.index.TextIndexed;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {

	private String civicNumber;
	
	@TextIndexed
	private String streetName;
	
	private String appartment;
	
	@TextIndexed
	private String city;
	
	private String state;
	
	private String zipCode;
	
}
