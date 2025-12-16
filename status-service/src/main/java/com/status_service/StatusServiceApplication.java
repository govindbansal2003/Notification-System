package com.status_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class StatusServiceApplication {

	public static void main(String[] args) {
		log.info("Starting Status-Service Application...");
		SpringApplication.run(StatusServiceApplication.class, args);
	}

}
