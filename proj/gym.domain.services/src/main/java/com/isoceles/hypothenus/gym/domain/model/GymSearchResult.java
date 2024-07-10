package com.isoceles.hypothenus.gym.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GymSearchResult {

	private String gymId;
	
	private String name;
	
	private String email;
	
	private Address address;
	
	private boolean isActive;
}
