package com.isoceles.hypothenus.gym.admin.papi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("com.isoceles.hypothenus.gym.domain.repository")
public class AppConfig {

//	@Bean
//	GymService instanciateGymService(GymRepository gymRepository) {
//		return new GymService(gymRepository);
//	}
//	
//	@Bean
//	GymRepository instanciateGymRepository() {
//		return new GymRepository();
//	}
}
