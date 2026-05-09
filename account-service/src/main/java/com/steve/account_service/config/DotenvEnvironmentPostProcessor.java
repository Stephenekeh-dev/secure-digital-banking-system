package com.steve.account_service.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String ENV_FILE_PATH = "C:/Users/User/Desktop/banking-system";
    private static final String PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(ENV_FILE_PATH)
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> properties = new HashMap<>();
            for (DotenvEntry entry : dotenv.entries()) {
                properties.put(entry.getKey(), entry.getValue());
            }

            // Add with LOWEST priority so command line args and
            // application.yml can still override if needed
            environment.getPropertySources()
                    .addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));

            System.out.println("✅ .env file loaded from: " + ENV_FILE_PATH);

        } catch (Exception e) {
            System.out.println("⚠️ Could not load .env file: " + e.getMessage());
        }
    }
}