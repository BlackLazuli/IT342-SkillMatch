package edu.cit.skillmatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Allow CORS for API endpoints
                registry.addMapping("/api/**")
                        .allowedOrigins(
                            "http://localhost:5173", // Allow React frontend
                            "http://10.0.2.2:8080",  // Allow Android emulator
                            "http://localhost:8080",
                            "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080", // Add EC2 URL
                            "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com"      // Allow local testing
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);

                // Allow CORS for static uploads (profile pictures)
                registry.addMapping("/uploads/**")
                        .allowedOrigins(
                            "http://localhost:5173", // Allow React frontend
                            "http://10.0.2.2:8080",  // Allow Android emulator
                            "http://localhost:8080",
                            "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080", // Add EC2 URL
                            "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com"      // Add without port
                        )
                        .allowedMethods("GET", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
