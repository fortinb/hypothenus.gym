package com.iso.hypo.gym.domain.model.search;

import com.iso.hypo.gym.domain.model.location.Address;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GymSearchResult {
	
	private String brandId;

	private String gymId;
	
	private String name;
	
	private String email;
	
	private Address address;
	
	private boolean isActive;
}
