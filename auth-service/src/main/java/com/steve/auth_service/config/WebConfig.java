package com.steve.auth_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:C:/Users/User/Desktop/banking-system/uploads/}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize()
                .toString()
                .replace("\\", "/");

        if (!absolutePath.endsWith("/")) {
            absolutePath = absolutePath + "/";
        }

        String location = "file:///" + absolutePath;

        System.out.println("Serving uploads from: " + location);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location);
    }
}