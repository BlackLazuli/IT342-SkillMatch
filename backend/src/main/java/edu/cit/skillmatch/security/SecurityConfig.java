package edu.cit.skillmatch.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource()) // Enable CORS
                .and()
                .csrf().disable()
                .authorizeRequests(auth -> auth
                        .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/users/**").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cors configuration to allow requests from frontend
    @Bean
public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    
    // Add allowed origins, including frontend localhost and Android
    config.addAllowedOrigin("http://localhost:5173"); // Web frontend
    config.addAllowedOrigin("http://10.0.2.2:8080"); // Android emulator

    config.addAllowedOrigin("http://10.0.2.2"); // Android emulator without port
    
    // Add more origins for Android
    config.addAllowedOrigin("capacitor://localhost");
    config.addAllowedOrigin("ionic://localhost");
    config.addAllowedOrigin("http://localhost");
    config.addAllowedOrigin("http://localhost:8080");
    

    config.addAllowedMethod("*");  // Allow all HTTP methods
    config.addAllowedHeader("*");  // Allow all headers
    config.setAllowCredentials(false);  // Change to false to simplify testing
    
    source.registerCorsConfiguration("/**", config);
    return source;
}
}
