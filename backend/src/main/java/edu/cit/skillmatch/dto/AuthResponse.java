package edu.cit.skillmatch.dto;

public class AuthResponse {
    private String email;
    private Long userId;
    private String token;
    private String role;
    private String firstName;
    private String lastName;
    private String phoneNumber;  // ✅ Add phoneNumber

    // Constructor updated to accept phoneNumber
    public AuthResponse(String email, Long userId, String token, String role, 
                        String firstName, String lastName, String phoneNumber) {
        this.email = email;
        this.userId = userId;
        this.token = token;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;  // Set phoneNumber
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

    public String getPhoneNumber() {
        return phoneNumber;  // Getter for phoneNumber
    }
}
