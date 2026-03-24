package com.projetofmds.fraudchecker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FraudcheckerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudcheckerApplication.class, args);
	}

}
