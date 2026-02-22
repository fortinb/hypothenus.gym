package com.iso.hypo.admin.papi.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.iso.hypo.common.context.RequestContext;
import com.iso.hypo.domain.security.RoleEnum;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableMongoRepositories(basePackages = { "com.iso.hypo.repositories" })
public class AppConfig {

	private static final Pattern BRAND_UUID_PATTERN = Pattern.compile("/brands/(?!code)([^/]+)(?:/|$)");

	@Bean
	ModelMapper instanciateModelMapper() {
		ModelMapper mapper = new ModelMapper();

		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT).setFieldMatchingEnabled(true);

		return mapper;
	}

	@Bean
	@Primary
	ObjectMapper instanciateObjectMapper() {

		ObjectMapper mapper = JsonMapper.builder().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.addModule(new PageJacksonModule()).addModule(new SortJacksonModule()).addModule(new JavaTimeModule())
				.build();

		return mapper;
	}

	@Bean
	@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
	RequestContext requestContext(HttpServletRequest request, ObjectMapper objectMapper, Environment env) {
		String trackingNumber = request.getHeader("x-tracking-number");

		// extract brandUuid from path /brands/{brandUuid}, excluding /brands/code/...
		String requestUri = request.getRequestURI();
		Matcher m = BRAND_UUID_PATTERN.matcher(requestUri);
		String brandUuid = m.find() ? m.group(1) : null;

		List<RoleEnum> roles = null;
		String username = null;

		// First, attempt to read from SecurityContext (normal runtime)
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null) {
				if (auth.getName() != null && !auth.getName().isBlank()) {
					username = auth.getName();
				}

				// map authorities -> RoleEnum
				List<RoleEnum> collected = new ArrayList<>();
				for (GrantedAuthority ga : auth.getAuthorities()) {
					if (ga == null)
						continue;
					String authority = ga.getAuthority();
					if (authority == null)
						continue;
					String norm = authority;
					if (norm.startsWith("ROLE_"))
						norm = norm.substring(5);
					else if (norm.startsWith("ROLE-"))
						norm = norm.substring(5);
					norm = norm.toLowerCase();
					try {
						RoleEnum re = RoleEnum.valueOf(norm);
						collected.add(re);
					} catch (IllegalArgumentException iae) {
						// ignore unknown role
					}
				}
				if (!collected.isEmpty())
					roles = collected;

			}
		} catch (Exception e) {
			// if security context not present or other errors, leave username/roles null
			// and allow fallback in test
		}

		return new RequestContext(username, trackingNumber, brandUuid, roles);
	}

}