package com.iso.hypo.admin.papi.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.parameters.HeaderParameter;

@Configuration
public class SpringDocConfig {

	@Bean
	GroupedOpenApi publicApi() {
		return GroupedOpenApi.builder().group("add-auth-header").addOperationCustomizer((operation, _) -> {
			operation
					.addParametersItem(
						new HeaderParameter().name("Authorization").description("Bearer <token>").required(true))
					.addParametersItem(
						new HeaderParameter().name("x-tracking-number").description("RequestId").required(false));
			return operation;
		}).build();
	}
}