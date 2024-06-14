package com.isoceles.hypothenus.gym.admin.papi.config;

import org.modelmapper.ModelMapper;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@EnableMongoRepositories("com.isoceles.hypothenus.gym.domain.repository")
public class AppConfig {
	
	@Bean
    ModelMapper instanciateModelMapper() {
        return new ModelMapper();
    }
	
	@Bean
    ObjectMapper instanciateObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new PageJacksonModule());
		mapper.registerModule(new SortJacksonModule());
		
		return mapper;
    }
}
