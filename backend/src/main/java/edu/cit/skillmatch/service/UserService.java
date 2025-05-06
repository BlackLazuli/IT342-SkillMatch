package edu.cit.skillmatch.service;

import edu.cit.skillmatch.repository.UserRepository;
import edu.cit.skillmatch.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Directory for storing profile pictures
    @Value("${profile.picture.upload.dir}")
    private String PROFILE_PICTURE_UPLOAD_DIR;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }

    public Optional<UserEntity> updateUser(Long id, UserEntity updatedUser) {
        return userRepository.findById(id).map(user -> {
            if (updatedUser.getFirstName() != null) {
                user.setFirstName(updatedUser.getFirstName());
            }
            if (updatedUser.getLastName() != null) {
                user.setLastName(updatedUser.getLastName());
            }
            if (updatedUser.getEmail() != null) {
                user.setEmail(updatedUser.getEmail());
            }
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setNewPassword(passwordEncoder, updatedUser.getPassword());
            }
            if (updatedUser.getRole() != null) {
                user.setRole(updatedUser.getRole());
            }
            if (updatedUser.getBio() != null) {
                user.setBio(updatedUser.getBio());
            }
            if (updatedUser.getPhoneNumber() != null) {
                user.setPhoneNumber(updatedUser.getPhoneNumber());
            }
            if (updatedUser.getLocation() != null) {
                user.setLocation(updatedUser.getLocation());
            }
            if (updatedUser.getRating() != null) {
                user.setRating(updatedUser.getRating());
            }
            return userRepository.save(user);
        });
    }

    // Method to upload profile picture after the user is created
    public UserEntity uploadProfilePicture(Long userId, MultipartFile file) throws IOException {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();

            if (file.isEmpty()) {
                throw new IllegalArgumentException("No file uploaded");
            }

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                throw new IllegalArgumentException("Invalid file name");
            }

            // Generate a unique file name to avoid name collisions
            String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;

            // Ensure directory exists
            File uploadDir = new File(PROFILE_PICTURE_UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Save the file to the local filesystem
            File destinationFile = new File(uploadDir, uniqueFileName);
            file.transferTo(destinationFile);

            // ✅ Store the *web-accessible path*, not the full system path
            String webPath = "/uploads/profile-pictures/" + uniqueFileName;
            user.setProfilePicture(webPath);

            return userRepository.save(user);
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }
    
    // Method to handle Base64 encoded profile pictures from mobile app
    public UserEntity uploadProfilePictureBase64(Long userId, String base64Image) {
        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            
            if (base64Image == null || base64Image.isEmpty()) {
                throw new IllegalArgumentException("No image data provided");
            }
            
            // Store the Base64 string directly in the database
            // This is more efficient for mobile apps than file storage
            user.setProfilePicture(base64Image);
            
            return userRepository.save(user);
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
