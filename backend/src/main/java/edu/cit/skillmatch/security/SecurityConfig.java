package edu.cit.skillmatch.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
        .cors().configurationSource(corsConfigurationSource()).and()
        .csrf().disable()
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/auth/**",
                "/api/users/**",
                "/uploads/**",
                "/api/appointments/**",
                "/uploads/profile-pictures/**", 
                "/v3/api-docs/**",
                "/swagger-ui/**"
            ).permitAll()
             .requestMatchers(HttpMethod.POST, "/api/appointments/").permitAll() 
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}

    // Cors configuration to allow requests from frontend
@Bean
public UrlBasedCorsConfigurationSource corsConfigurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration config = new CorsConfiguration();
    
    // Add all necessary origins (including EC2 URLs)
    config.setAllowedOrigins(Arrays.asList(
        "http://localhost:5173",
        "http://10.0.2.2:8080",
        "http://10.0.2.2",
        "capacitor://localhost",
        "ionic://localhost",
        "http://localhost",
        "http://localhost:8080",
        "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com",
        "http://ec2-3-107-23-86.ap-southeast-2.compute.amazonaws.com:8080",
        "https://it-342-skill-match.vercel.app" // Add this for Vercel frontend
    ));
    
    config.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);
    
    // Apply to all paths
    source.registerCorsConfiguration("/**", config);
    return source;
}
}
