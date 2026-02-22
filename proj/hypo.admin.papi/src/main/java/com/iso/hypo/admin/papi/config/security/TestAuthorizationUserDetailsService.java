package com.iso.hypo.admin.papi.config.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class TestAuthorizationUserDetailsService
		implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	@Override
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) {
		final TestAuthorizationDto principal = (TestAuthorizationDto) token.getPrincipal();
		final String credentials = (String) token.getCredentials();

		final Collection<SimpleGrantedAuthority> authorities = principal.getRoles().stream()
				.map(i -> new SimpleGrantedAuthority("ROLE_".concat(i.toString()))).collect(Collectors.toList());
		
		return new User(credentials, "", authorities);
	}
}
