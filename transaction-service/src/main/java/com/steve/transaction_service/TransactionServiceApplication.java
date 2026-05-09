package com.steve.transaction_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransactionServiceApplication {

	public static void main(String[] args) {
		loadEnv();
		SpringApplication.run(TransactionServiceApplication.class, args);
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