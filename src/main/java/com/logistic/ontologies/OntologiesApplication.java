package com.logistic.ontologies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.logistic.ontologies", "com.logistic.ontologies.config"})
public class OntologiesApplication {

	public static void main(String[] args) {
		SpringApplication.run(OntologiesApplication.class, args);
	}
}
