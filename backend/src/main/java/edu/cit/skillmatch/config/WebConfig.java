package edu.cit.skillmatch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${profile.picture.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Add explicit trailing slash and ensure correct path mapping
        String externalLocation = "file:" + uploadDir;
        if (!uploadDir.endsWith("/")) {
            externalLocation += "/";
        }
        
        registry.addResourceHandler("/uploads/profile-pictures/**")
                .addResourceLocations(externalLocation)
                .setCachePeriod(3600);
    }
}