package com.isoceles.hypothenus.gym.admin.papi.config.security;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorizationDto {
	private List<String> roles;
}
