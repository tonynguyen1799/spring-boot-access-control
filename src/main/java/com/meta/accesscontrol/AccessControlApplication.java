package com.meta.accesscontrol;

import com.meta.accesscontrol.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware") // Enable JPA Auditing
@EnableConfigurationProperties(JwtProperties.class) // Enable Type-Safe Properties
public class AccessControlApplication {
	public static void main(String[] args) {
		SpringApplication.run(AccessControlApplication.class, args);
	}
}