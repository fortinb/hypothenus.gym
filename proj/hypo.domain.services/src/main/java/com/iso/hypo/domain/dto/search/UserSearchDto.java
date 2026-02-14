package com.iso.hypo.domain.dto.search;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchDto {

    private String uuid;

	private String firstname;
	
	private String lastname;
	
	private String email;
    
    private boolean isActive;
}