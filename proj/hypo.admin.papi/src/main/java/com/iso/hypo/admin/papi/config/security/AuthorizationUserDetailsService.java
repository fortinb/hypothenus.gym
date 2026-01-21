package com.iso.hypo.admin.papi.config.security;

import java.util.Collection;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthorizationUserDetailsService
		implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
	
	@Autowired
	ObjectMapper objectMapper;

	@Override
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) {
		final String principal = (String) token.getPrincipal();
		final String credentials = (String) token.getCredentials();

		
		try {
			AuthorizationDto authorizationDto = objectMapper.readValue(principal, new TypeReference<AuthorizationDto>(){});
			final Collection<SimpleGrantedAuthority> authorities = authorizationDto.getRoles().stream()
					.map(i -> new SimpleGrantedAuthority("ROLE_".concat(i))).collect(Collectors.toList());
			return new User(credentials, "", authorities);
		} catch (JsonMappingException e) {
			e.printStackTrace();
			return null;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
