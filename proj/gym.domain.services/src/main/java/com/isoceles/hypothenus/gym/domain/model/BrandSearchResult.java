package com.isoceles.hypothenus.gym.domain.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandSearchResult {
	
	private String brandId;

	private String name;
	
	private String email;
	
	private Address address;
	
	private boolean isActive;
}
