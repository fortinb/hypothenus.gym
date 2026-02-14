package com.iso.hypo.domain.dto;

import java.util.List;

import com.iso.hypo.common.dto.BaseEntityDto;
import com.iso.hypo.domain.security.Roles;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto extends BaseEntityDto {

	private String uuid;
	
	private String firstname;

	private String lastname;
	
	private String email;
	
	private List<Roles> roles;
}
