package com.pair.pair_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PairBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PairBackendApplication.class, args);
	}

}
