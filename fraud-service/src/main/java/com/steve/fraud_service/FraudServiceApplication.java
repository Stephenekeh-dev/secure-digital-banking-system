package com.steve.fraud_service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class FraudServiceApplication {

	public static void main(String[] args) {
		loadEnv();
		SpringApplication.run(FraudServiceApplication.class, args);
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