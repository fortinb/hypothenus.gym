package com.isoceles.hypothenus.gym.admin.papi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggerConfig {
	
	public class LoggerProvider {
		
	    @Bean
	    Logger logger() {
	        return LoggerFactory.getLogger("Isoceles.Hypothenus.Gym");
	    }
	}
}
