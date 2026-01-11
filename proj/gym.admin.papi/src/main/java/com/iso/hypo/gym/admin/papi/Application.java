package com.iso.hypo.gym.admin.papi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.iso.hypo.gym.admin.papi.config.CorsProperties;

@SpringBootApplication()
@ComponentScan({ 
		"com.iso.hypo.gym.admin", 
		"com.iso.hypo.brand", 
		"com.iso.hypo.gym",
		"com.iso.hypo.member",
		"com.iso.hypo.common"
})
@EnableConfigurationProperties(CorsProperties.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
