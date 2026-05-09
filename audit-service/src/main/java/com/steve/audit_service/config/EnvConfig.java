package com.steve.audit_service.config;





import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class EnvConfig {

    @PostConstruct
    public void loadEnv() {
        try {
            System.out.println("🔍 Looking for .env file...");

            // Try multiple locations
            Dotenv dotenv = null;

            // Try parent directory (banking-system root)
            try {
                dotenv = Dotenv.configure()
                        .directory("../")
                        .ignoreIfMissing()
                        .load();
                System.out.println("✅ Loaded .env from parent directory");
            } catch (Exception e) {
                System.out.println("❌ Could not load from parent directory: " + e.getMessage());
            }

            // Try current directory if parent didn't work
            if (dotenv == null || dotenv.entries().isEmpty()) {
                try {
                    dotenv = Dotenv.configure()
                            .directory(".")
                            .ignoreIfMissing()
                            .load();
                    System.out.println("✅ Loaded .env from current directory");
                } catch (Exception e) {
                    System.out.println("❌ Could not load from current directory: " + e.getMessage());
                }
            }

            // Try absolute path
            if (dotenv == null || dotenv.entries().isEmpty()) {
                try {
                    dotenv = Dotenv.configure()
                            .directory("C:/users/user/desktop/banking-system")
                            .ignoreIfMissing()
                            .load();
                    System.out.println("✅ Loaded .env from absolute path");
                } catch (Exception e) {
                    System.out.println("❌ Could not load from absolute path: " + e.getMessage());
                }
            }

            // If we found a .env file, load its values
            if (dotenv != null && !dotenv.entries().isEmpty()) {
                System.out.println("📝 Loading environment variables...");

                // Set database credentials specifically
                String dbUsername = dotenv.get("DB_USERNAME");
                String dbPassword = dotenv.get("DB_PASSWORD");
                String jwtSecret = dotenv.get("JWT_SECRET");

                if (dbUsername != null) {
                    System.setProperty("DB_USERNAME", dbUsername);
                    System.out.println("✅ Set DB_USERNAME: " + dbUsername);
                }

                if (dbPassword != null) {
                    System.setProperty("DB_PASSWORD", dbPassword);
                    System.out.println("✅ Set DB_PASSWORD: " + "*".repeat(dbPassword.length()));
                }

                if (jwtSecret != null) {
                    System.setProperty("JWT_SECRET", jwtSecret);
                    System.out.println("✅ Set JWT_SECRET");
                }

                // Load all other properties safely
                for (String key : new String[]{"SPRING_KAFKA_BOOTSTRAP_SERVERS", "MAIL_HOST", "MAIL_PORT", "MAIL_USERNAME", "MAIL_PASSWORD"}) {
                    String value = dotenv.get(key);
                    if (value != null) {
                        System.setProperty(key, value);
                        System.out.println("✅ Set " + key);
                    }
                }
            } else {
                System.out.println("⚠️ No .env file found, using default values");
            }

        } catch (Exception e) {
            System.err.println("❌ Error loading .env file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}