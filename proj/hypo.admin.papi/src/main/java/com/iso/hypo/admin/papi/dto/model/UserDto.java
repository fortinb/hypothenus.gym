package com.iso.hypo.admin.papi.dto.model;

import java.util.List;

import com.iso.hypo.admin.papi.dto.BaseDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto extends BaseDto {

	private String uuid;
	
	private String firstname;

	private String lastname;
	
	private String email;
	
	private List<String> roles;
}
