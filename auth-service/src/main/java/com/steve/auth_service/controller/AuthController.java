package com.steve.auth_service.controller;

import com.steve.auth_service.security.JwtUtil;
import com.steve.auth_service.dto.AuthResponse;
import com.steve.auth_service.dto.LoginRequest;
import com.steve.auth_service.dto.RegisterRequest;
import com.steve.auth_service.model.User;
import com.steve.auth_service.kafka.AuditEventProducer;
import com.steve.auth_service.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditEventProducer auditEventProducer;

    @Value("${file.upload-dir:C:/Users/User/Desktop/banking-system/uploads/}")
    private String uploadDir;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(@Valid @ModelAttribute RegisterRequest request) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already registered"));
            }

            // Save profile picture
            String relativePath = saveProfilePicture(request.getProfilePicture());

            // Create and save user
            User user = User.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .profilePictureUrl(relativePath)
                    .role("USER")
                    .build();

            userRepository.save(user);
            auditEventProducer.sendRegistrationAudit(user.getEmail());
            log.info("User registered successfully: {}", user.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User registered successfully. Please log in."));

        } catch (Exception e) {
            log.error("Registration error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .map(user -> {
                    String token = jwtUtil.generateToken(user.getEmail());
                    auditEventProducer.sendLoginAudit(user.getEmail(), true);
                    log.info("Login success: {}", user.getEmail());
                    return ResponseEntity.ok((Object) AuthResponse.builder()
                            .token(token)
                            .email(user.getEmail())
                            .fullName(user.getFullName())
                            .profilePictureUrl(user.getProfilePictureUrl())
                            .build());
                })
                .orElseGet(() -> {
                    auditEventProducer.sendLoginAudit(request.getEmail(), false);
                    log.warn("Login failed for: {}", request.getEmail());
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Invalid email or password"));
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String saveProfilePicture(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Build absolute path and create directories if missing
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath);
        }

        // Sanitize filename — remove unsafe characters
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "file";
        }
        String safeFilename = System.currentTimeMillis() + "_"
                + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

        Path destination = uploadPath.resolve(safeFilename);

        // Copy file using NIO — avoids Tomcat temp directory issues
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }

        log.info("Profile picture saved: {}", destination.toAbsolutePath());
        return "/uploads/" + safeFilename;
    }
}