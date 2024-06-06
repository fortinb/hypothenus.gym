package com.isoceles.hypothenus.gym.admin.papi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.isoceles.hypothenus.gym.domain.repository.GymRepository;
import com.isoceles.hypothenus.gym.domain.services.GymService;

@Configuration
public class AppConfig {

	@Bean
	GymService instanciateGymService(GymRepository gymRepository) {
		return new GymService(gymRepository);
	}
	
	@Bean
	GymRepository instanciateGymRepository() {
		return new GymRepository();
	}
}
