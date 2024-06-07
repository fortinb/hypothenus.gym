package com.isoceles.hypothenus.gym.admin.papi.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("com.isoceles.hypothenus.gym.domain.repository")
public class AppConfig {
	
	@Bean
    public ModelMapper instanciateModelMapper() {
        return new ModelMapper();
    }
}
