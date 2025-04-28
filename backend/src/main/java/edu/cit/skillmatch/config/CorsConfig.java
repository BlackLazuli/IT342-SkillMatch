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
                            "http://localhost:5173", // Local React
                            "http://10.0.2.2:8080",  // Android emulator
                            "http://localhost:8080", // Local backend
                            "https://it-342-skill-match.vercel.app" // Vercel frontend
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
    
                // Allow CORS for static uploads (profile pictures)
                registry.addMapping("/uploads/**")
                .allowedOrigins(
                    "http://localhost:5173",
                    "http://10.0.2.2:8080",
                    "http://localhost:8080",
                    "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com",
                    "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080",
                    "https://it-342-skill-match.vercel.app"
                )
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);  // Add this line if credentials are needed
        
            }
        };
    }
    
}
