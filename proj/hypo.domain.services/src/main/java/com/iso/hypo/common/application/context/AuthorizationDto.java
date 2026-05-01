package com.iso.hypo.common.application.context;

import java.util.List;

import com.iso.hypo.common.domain.model.enumeration.RoleEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationDto {
	private List<RoleEnum> roles;
}
