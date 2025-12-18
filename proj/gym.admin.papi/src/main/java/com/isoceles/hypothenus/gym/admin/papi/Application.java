package com.isoceles.hypothenus.gym.admin.papi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.isoceles.hypothenus.gym.admin.papi.config.CorsProperties;

@SpringBootApplication
@ComponentScan({"com.isoceles.hypothenus.gym.admin", "com.isoceles.hypothenus.gym.domain"})
@EnableConfigurationProperties(CorsProperties.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
