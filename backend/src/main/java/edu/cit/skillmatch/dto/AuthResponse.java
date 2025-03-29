package edu.cit.skillmatch.dto;

public class AuthResponse {
    private String email;
    private Long userId;
    private String token;
    private String role; // Add role field

    public AuthResponse(String email, Long userId, String token, String role) {
        this.email = email;
        this.userId = userId;
        this.token = token;
        this.role = role;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public String getRole() {
        return role;
    }
}
