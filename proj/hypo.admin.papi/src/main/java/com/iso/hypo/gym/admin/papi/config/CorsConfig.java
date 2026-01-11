package com.iso.hypo.gym.admin.papi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
		CorsConfiguration config = new CorsConfiguration();

		config.setAllowedOrigins(props.getAllowedOrigins());
		config.setAllowedMethods(props.getAllowedMethods());
		config.setAllowedHeaders(props.getAllowedHeaders());
		config.setAllowCredentials(props.isAllowCredentials());
		config.setMaxAge(props.getMaxAge());

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration("/**", config);
		return source;
	}

}