package com.iso.hypo.brand.application.dto;

import java.util.List;

import com.iso.hypo.common.application.dto.BaseEntityDto;
import com.iso.hypo.common.application.security.RoleEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto extends BaseEntityDto {

	private String uuid;
	
/*	private String idpId;

	private String upn;
	*/
	private String firstname;

	private String lastname;
	
	private String email;
	
	private List<RoleEnum> roles;
}
