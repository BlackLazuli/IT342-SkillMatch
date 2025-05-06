package edu.cit.skillmatch.controller;

import edu.cit.skillmatch.dto.AuthRequest;
import edu.cit.skillmatch.dto.AuthResponse;
import edu.cit.skillmatch.entity.UserEntity;
import edu.cit.skillmatch.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://10.0.2.2:8080"})
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Optional<UserEntity> userOpt = userRepository.findByEmail(request.getEmail());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        
        UserEntity user = userOpt.get();
        
        if (!user.checkPassword(passwordEncoder, request.getPassword())) {
            return ResponseEntity.status(401).build();
        }
        
        AuthResponse response = new AuthResponse(
            user.getEmail(),
            user.getId(),
            null, // No token
            user.getRole(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber()
        );
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody UserEntity user) {
        try {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                return ResponseEntity.status(409).build();
            }
            
            user.setNewPassword(passwordEncoder, user.getPassword());
            
            if (user.getRole() == null) {
                user.setRole("CUSTOMER");
            }
            
            UserEntity savedUser = userRepository.save(user);
            
            AuthResponse response = new AuthResponse(
                savedUser.getEmail(),
                savedUser.getId(),
                null,
                savedUser.getRole(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getPhoneNumber()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}