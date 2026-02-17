package com.iso.hypo.admin.papi.config.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.cors(Customizer.withDefaults())
				.addFilterBefore(authorizationFilter(), RequestHeaderAuthenticationFilter.class)
				.csrf((i) -> i.disable()).authorizeHttpRequests((authorize) -> authorize
						.requestMatchers("/swagger-ui/**").anonymous().requestMatchers("/*/api-docs/**").anonymous()
						.anyRequest().authenticated());

		return http.build();
	}

	@Bean(name = "authorizationFilter")
	RequestHeaderAuthenticationFilter authorizationFilter() {

		RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter = new RequestHeaderAuthenticationFilter();

		requestHeaderAuthenticationFilter.setPrincipalRequestHeader("x-authorization");
		requestHeaderAuthenticationFilter.setCredentialsRequestHeader("x-credentials");

		requestHeaderAuthenticationFilter.setAuthenticationManager(authenticationManager());

		requestHeaderAuthenticationFilter.setExceptionIfHeaderMissing(false);

		return requestHeaderAuthenticationFilter;
	}

	@Bean(name = "customAuthenticationManager")
	AuthenticationManager authenticationManager() {
		final List<AuthenticationProvider> providers = new ArrayList<>(1);
		providers.add(preAuthCustomProvider());
		return new ProviderManager(providers);
	}

	@Bean(name = "preAuthProvider")
	PreAuthenticatedAuthenticationProvider preAuthCustomProvider() {
		PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
		provider.setPreAuthenticatedUserDetailsService(userDetailsService());
		return provider;
	}

	@Bean
	AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> userDetailsService() {
		return new AuthorizationUserDetailsService();
	}
}
