package org.devconnect.devconnectbackend.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.devconnect.devconnectbackend.dto.UserDTO;
import org.devconnect.devconnectbackend.dto.UserRegistrationDTO;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * Create a new user (temporary - for testing without auth)
     * POST /api/users/register
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@RequestBody UserRegistrationDTO registrationDTO) {
        try {
            // Check if user already exists
            if (userRepository.existsByEmail(registrationDTO.getEmail())) {
                return ResponseEntity.badRequest().build();
            }
            
            if (userRepository.existsByUsername(registrationDTO.getUsername())) {
                return ResponseEntity.badRequest().build();
            }
            
            // Create user
            User user = new User();
            user.setUsername(registrationDTO.getUsername());
            user.setEmail(registrationDTO.getEmail());
            user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
            String role = registrationDTO.getRole();
            if (role == null) {
                return ResponseEntity.badRequest().build();
            }
            user.setRole(User.UserRole.valueOf(role.toUpperCase()));
            user.setStatus(User.UserStatus.OFFLINE);
            
            user = userRepository.save(user);
            
            return ResponseEntity.ok(convertToDTO(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Simple login (temporary - returns user info without JWT)
     * POST /api/users/login
     */
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Check password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(401).build();
            }
            
            return ResponseEntity.ok(convertToDTO(user));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }
    
    /**
     * Get user by ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(convertToDTO(user));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all users (for testing)
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }
    
    /**
     * Get users by role
     * GET /api/users/role/{role}
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            List<User> users = userRepository.findAll().stream()
                    .filter(user -> user.getRole() == userRole)
                    .collect(Collectors.toList());
            
            List<UserDTO> userDTOs = users.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    private UserDTO convertToDTO(User user) {
    UserDTO dto = new UserDTO(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getRole().name().toLowerCase(),
        user.getStatus().name().toLowerCase(),
        user.getAvatar()
    );
    return dto;
    }
}
