package com.example.googleCalendarSync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class GoogleCalendarSyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoogleCalendarSyncApplication.class, args);
	}

}
