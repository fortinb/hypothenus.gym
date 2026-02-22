package com.iso.hypo.admin.papi.config.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthConverter,
				ObjectProvider<RequestHeaderAuthenticationFilter> headerFilterProvider,
				ObjectProvider<PreAuthBrandFilter> authorizationHeaderPreAuthFilterProvider) throws Exception {
		http.cors(Customizer.withDefaults());

		// If header-based filter is available (test profile), register it. Otherwise register AuthorizationHeaderPreAuthFilter
		RequestHeaderAuthenticationFilter headerFilter = headerFilterProvider.getIfAvailable();
		if (headerFilter != null) {
			http.addFilterBefore(headerFilter, UsernamePasswordAuthenticationFilter.class);

			// In test profile (header-based auth), DO NOT configure oauth2 resource server — tests don't provide JWTs
			http.csrf((i) -> i.disable()).authorizeHttpRequests((authorize) -> authorize
					.requestMatchers("/swagger-ui/**").anonymous().requestMatchers("/*/api-docs/**").anonymous()
					.anyRequest().authenticated());
		} else {
			// Normal runtime: use Authorization header JWT decoding/validation and resource-server support
			PreAuthBrandFilter authorizationHeaderPreAuthFilter = authorizationHeaderPreAuthFilterProvider.getIfAvailable();
			if (authorizationHeaderPreAuthFilter != null) {
				http.addFilterBefore(authorizationHeaderPreAuthFilter, UsernamePasswordAuthenticationFilter.class);
			}
			http.csrf((i) -> i.disable()).authorizeHttpRequests((authorize) -> authorize
					.requestMatchers("/swagger-ui/**").anonymous()
					.requestMatchers("/*/api-docs/**").anonymous()
					.requestMatchers("/*/brands/code/*").permitAll()
					.anyRequest().authenticated()).oauth2ResourceServer((rs) -> rs.jwt((jwt) -> jwt.jwtAuthenticationConverter(jwtAuthConverter)));
		}

		return http.build();
	}

	@Bean
	@Profile("!test")
	JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
		// Use Spring helper to build a decoder from the issuer's metadata (will fetch jwk-set-uri)
		return JwtDecoders.fromIssuerLocation(issuerUri);
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

		// custom converter to map Azure roles/groups claims into ROLE_ prefixed authorities
		converter.setJwtGrantedAuthoritiesConverter(new Converter<Jwt, Collection<GrantedAuthority>>() {
			@Override
			public Collection<GrantedAuthority> convert(Jwt jwt) {
				Collection<GrantedAuthority> authorities = new ArrayList<>();

				// roles claim (application roles)
				Object roles = jwt.getClaim("roles");
				if (roles instanceof Collection) {
					for (Object r : (Collection<?>) roles) {
						if (r != null) {
							authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toString()));
						}
					}
				}

				return authorities;
			}
		});

		return converter;
	}

	// Beans used only during tests - enable header pre-auth filter and provider
	@Bean
	@Profile("test")
	RequestHeaderAuthenticationFilter headerPreAuthFilter(AuthenticationManager authenticationManager) {
		RequestHeaderAuthenticationFilter filter = new TestHeaderPreAuthenticationFilter();
		filter.setPrincipalRequestHeader("x-authorization");
		filter.setCredentialsRequestHeader("x-credentials");
		filter.setExceptionIfHeaderMissing(false);
		filter.setAuthenticationManager(authenticationManager);
		return filter;
	}

	@Bean
	@Profile("test")
	PreAuthenticatedAuthenticationProvider preAuthProvider(AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> uds) {
		PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
		provider.setPreAuthenticatedUserDetailsService(uds);
		return provider;
	}

	@Bean
	@Profile("test")
	AuthenticationManager authenticationManager(PreAuthenticatedAuthenticationProvider preAuthProvider) {
		List<AuthenticationProvider> providers = new ArrayList<>();
		providers.add(preAuthProvider);
		return new ProviderManager(providers);
	}

	@Bean
	@Profile("test")
	AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> userDetailsService() {
		return new TestAuthorizationUserDetailsService();
	}

	@Bean
	@Profile("!test")
	PreAuthBrandFilter authorizationHeaderPreAuthFilter(JwtDecoder jwtDecoder) {
		return new PreAuthBrandFilter(jwtDecoder);
	}

	@Bean
	@Profile("test")
	PreAuthBrandFilter authorizationHeaderPreAuthFilterTest() {
		// Provide a lightweight stub filter to satisfy autowiring in tests. It won't be used because headerPreAuthFilter
		// (RequestHeaderAuthenticationFilter) is registered in the test profile. The JwtDecoder here is a no-op stub.
		JwtDecoder noopDecoder = new JwtDecoder() {
			@Override
			public Jwt decode(String token) {
				throw new UnsupportedOperationException("Jwt decoding not available in test profile");
			}
		};
		return new PreAuthBrandFilter(noopDecoder);
	}

}