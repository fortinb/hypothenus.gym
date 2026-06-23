package com.iso.hypo.admin.papi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.iso.hypo.admin.papi.config.CorsProperties;

@SpringBootApplication()
@ComponentScan({ 
		"com.iso.hypo.admin", 
		"com.iso.hypo.brand", 
		"com.iso.hypo.common", 
		"com.iso.hypo.sale",
		"com.iso.hypo.finance",
		"com.iso.hypo.membership",
		"com.iso.hypo.admin.papi.cache"
})
@EnableConfigurationProperties(CorsProperties.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
