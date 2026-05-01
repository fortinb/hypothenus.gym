package com.iso.hypo.admin.papi.config.security;

import java.util.List;

import com.iso.hypo.common.domain.model.enumeration.RoleEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestAuthorizationDto {
	private List<RoleEnum> roles;
}
