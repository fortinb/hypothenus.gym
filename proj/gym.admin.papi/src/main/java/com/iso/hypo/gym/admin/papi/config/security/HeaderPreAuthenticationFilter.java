package com.iso.hypo.gym.admin.papi.config.security;

import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;

public class HeaderPreAuthenticationFilter extends RequestHeaderAuthenticationFilter {

	public HeaderPreAuthenticationFilter() {
		super();

	}

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		String principal = request.getHeader("x-authorization");

		ObjectMapper mapper = new ObjectMapper();
		try {
			AuthorizationDto authorizationDto = mapper.readValue(principal, new TypeReference<AuthorizationDto>(){});
			return authorizationDto;
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		String credentials = request.getHeader("x-credentials");
		return credentials == null ? "n/a" : credentials;
	}

}
