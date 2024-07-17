package com.isoceles.hypothenus.gym.admin.papi.config;

import org.modelmapper.ModelMapper;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.isoceles.hypothenus.gym.domain.context.RequestContext;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableMongoRepositories("com.isoceles.hypothenus.gym.domain.repository")
public class AppConfig {
	
	@Bean
    ModelMapper instanciateModelMapper() {
        return new ModelMapper();
    }
	
	@Bean
	@Primary
    ObjectMapper instanciateObjectMapper() {
		
		ObjectMapper mapper = JsonMapper.builder()
					.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
					.addModule(new PageJacksonModule())
					.addModule(new SortJacksonModule())
					.addModule(new JavaTimeModule())
					.build();
		//mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		//mapper.registerModule(new PageJacksonModule());
		//mapper.registerModule(new SortJacksonModule());
		//mapper.registerModule(new JavaTimeModule());
		 
		return mapper;
    }

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    RequestContext requestContext(HttpServletRequest request) {
	    return new RequestContext(request);
	}
}
