package com.steve.auth_service;

import io.github.cdimascio.dotenv.Dotenv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class AuthServiceApplication {

	private static final Logger log = LoggerFactory.getLogger(AuthServiceApplication.class);

	public static void main(String[] args) {
		loadEnv();
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	private static void loadEnv() {
		try {
			Dotenv dotenv = Dotenv.configure()
					.directory("C:/Users/User/Desktop/banking-system")
					.ignoreIfMissing()
					.load();
			dotenv.entries().forEach(entry ->
					System.setProperty(entry.getKey(), entry.getValue())
			);
			System.out.println("env loaded successfully");
		} catch (Exception e) {
			System.out.println("Could not load .env file: " + e.getMessage());
		}
	}


}