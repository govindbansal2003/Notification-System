package com.notification_worker;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class NotificationWorkerApplication {

	public static void main(String[] args) {
		log.info("Starting Worker Application...");
		SpringApplication.run(NotificationWorkerApplication.class, args);
	}

}
