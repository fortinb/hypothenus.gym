package com.iso.hypo.common.context;

import java.util.List;

import com.iso.hypo.domain.security.RoleEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationDto {
	private List<RoleEnum> roles;
}
