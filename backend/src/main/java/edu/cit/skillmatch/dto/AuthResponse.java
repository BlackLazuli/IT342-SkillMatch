package edu.cit.skillmatch.dto;

public class AuthResponse {
    private String email;
    private Long userId;
    private String token;
    private String role;
    private String firstName; // Add firstName
    private String lastName;  // Add lastName

    public AuthResponse(String email, Long userId, String token, String role, String firstName, String lastName) {
        this.email = email;
        this.userId = userId;
        this.token = token;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
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

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
