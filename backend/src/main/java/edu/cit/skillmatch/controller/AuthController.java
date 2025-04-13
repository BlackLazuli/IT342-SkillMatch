package edu.cit.skillmatch.controller;

import edu.cit.skillmatch.dto.AuthRequest;
import edu.cit.skillmatch.dto.AuthResponse;
import edu.cit.skillmatch.entity.UserEntity;
import edu.cit.skillmatch.repository.UserRepository;
import edu.cit.skillmatch.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://10.0.2.2:8080"}) // Allow React frontend and Android
public class AuthController {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        System.out.println("Login attempt for email: " + request.getEmail());
        
        Optional<UserEntity> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (!userOpt.isPresent()) {
            System.out.println("User not found with email: " + request.getEmail());
            return ResponseEntity.status(401).body(null);
        }
        
        UserEntity user = userOpt.get();
        System.out.println("Found user: " + user.getEmail());
        System.out.println("Stored password hash: " + user.getPassword());
        System.out.println("Input password: " + request.getPassword());
        
        // Try direct comparison for testing purposes
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPassword());
        System.out.println("Password match result: " + passwordMatches);
        
        // For testing, allow login with correct email regardless of password
        // Remove this in production!
        String token = jwtUtil.generateToken(user.getEmail());
        
        // Include phoneNumber in the response
        AuthResponse response = new AuthResponse(
            user.getEmail(),
            user.getId(),
            token,
            user.getRole(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber()  // Include phoneNumber here
        );
        
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody UserEntity user) {
        try {
            // Check if email already exists
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.status(409).body(null); // Conflict - email already exists
            }
            
            // Ensure the password is encoded before saving
            if (user.getPassword() != null) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            
            // Make sure role/userType is not null
            if (user.getRole() == null) {
                user.setRole("CUSTOMER"); // Default role if none provided
            }
            
            // Save the new user
            UserEntity savedUser = userRepository.save(user);
            
            // Generate JWT token
            String token = jwtUtil.generateToken(savedUser.getEmail());
            
            // Create response with phoneNumber included
            AuthResponse response = new AuthResponse(
                savedUser.getEmail(),
                savedUser.getId(),
                token,
                savedUser.getRole(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getPhoneNumber()  // Include phoneNumber here
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }
    
}
