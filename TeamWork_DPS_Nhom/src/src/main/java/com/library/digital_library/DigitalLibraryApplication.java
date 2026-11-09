package com.library.digital_library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DigitalLibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(DigitalLibraryApplication.class, args);
	}

}
