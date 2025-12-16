package com.NotificationApp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class NotificationAppApplication {

	public static void main(String[] args) {
		log.info("Starting Notification Application...");
		SpringApplication.run(NotificationAppApplication.class, args);
	}

}
