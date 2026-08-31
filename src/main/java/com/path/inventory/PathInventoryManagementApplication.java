package com.path.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PathInventoryManagementApplication {

	public static void main(String[] args) {

		SpringApplication.run(
				PathInventoryManagementApplication.class,
				args);
	}

}
