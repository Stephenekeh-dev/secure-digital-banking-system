package com.steve.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.LinkedHashMap;

@RestController
public class SwaggerLinksController {

    @GetMapping("/docs")
    public ResponseEntity<Map<String, String>> getSwaggerLinks() {
        Map<String, String> links = new LinkedHashMap<>();
        links.put("auth-service", "http://localhost:8081/swagger-ui/index.html");
        links.put("account-service", "http://localhost:8082/swagger-ui/index.html");
        links.put("transaction-service", "http://localhost:8083/swagger-ui/index.html");
        links.put("notification-service", "http://localhost:8084/swagger-ui/index.html");
        links.put("audit-service", "http://localhost:8085/swagger-ui/index.html");
        links.put("fraud-service", "http://localhost:8086/swagger-ui/index.html");
        links.put("approval-service", "http://localhost:8087/swagger-ui/index.html");
        return ResponseEntity.ok(links);
    }
}