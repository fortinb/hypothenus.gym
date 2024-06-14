package com.isoceles.hypothenus.gym.domain.model;

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
	
	private String zipCode;
	
	public Address() {
	}
	
	public Address(String civicNumber, String streetName, String appartment, String city, String state,	String zipCode) {
		super();
		this.civicNumber = civicNumber;
		this.streetName = streetName;
		this.appartment = appartment;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
	}


	
}
