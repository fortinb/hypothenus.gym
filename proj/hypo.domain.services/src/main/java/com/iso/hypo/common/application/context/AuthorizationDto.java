package com.iso.hypo.common.application.context;

import java.util.List;

import com.iso.hypo.common.application.security.RoleEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationDto {
	private List<RoleEnum> roles;
}
