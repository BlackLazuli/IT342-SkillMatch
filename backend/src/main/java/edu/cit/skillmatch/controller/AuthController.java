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
@CrossOrigin(origins = "http://localhost:5173") // Allow React frontend
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
        Optional<UserEntity> userOpt = userRepository.findByEmail(request.getEmail());
    
        if (userOpt.isPresent() && passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            UserEntity user = userOpt.get();
            String token = jwtUtil.generateToken(user.getEmail());
    
            // Include role in the response
            AuthResponse response = new AuthResponse(user.getEmail(), user.getId(), token, user.getRole());
            return ResponseEntity.ok(response);
        }
    
        return ResponseEntity.status(401).body(null);
    }
}
