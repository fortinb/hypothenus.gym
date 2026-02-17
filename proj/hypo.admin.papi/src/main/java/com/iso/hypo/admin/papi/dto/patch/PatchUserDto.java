package com.iso.hypo.admin.papi.dto.patch;

import java.util.List;

import com.iso.hypo.admin.papi.dto.BaseDto;
import com.iso.hypo.admin.papi.dto.enumeration.RoleEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchUserDto extends BaseDto {

	private String uuid;
	
	private String firstname;

	private String lastname;
	
	private String email;
	
	private List<RoleEnum> roles;
}
