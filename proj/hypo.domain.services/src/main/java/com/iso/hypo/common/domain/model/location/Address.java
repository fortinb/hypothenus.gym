package com.iso.hypo.common.domain.model.location;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Address {
	
	private String civicNumber;
	
	private String streetName;
	
	private String appartment;
	
	private String city;
	
	private String state;
	
	private String country;
	
	private String zipCode;
	
	public Address() {
	}
	
	public Address(String civicNumber, String streetName, String appartment, String city, String country, String state,	String zipCode) {
		super();
		this.civicNumber = civicNumber;
		this.streetName = streetName;
		this.appartment = appartment;
		this.city = city;
		this.state = state;
		this.country = country;
		this.zipCode = zipCode;
	}
}
