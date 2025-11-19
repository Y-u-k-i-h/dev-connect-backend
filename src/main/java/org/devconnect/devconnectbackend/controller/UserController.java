package org.devconnect.devconnectbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.devconnect.devconnectbackend.dto.*;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

    // User Registration
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {
        UserResponseDTO createdUser = userService.registerUser(userRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // User Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(HttpServletRequest request, @Valid @RequestBody LoginDTO loginDTO) {
        // Log login attempt for debugging (do not log passwords in production)
        String origin = request.getHeader("Origin");
        logger.info("Login attempt from origin {} for email={}", origin, loginDTO.getEmail());

        LoginResponseDTO loginResponseDTO = userService.login(loginDTO);
        return ResponseEntity.status(HttpStatus.OK).body(loginResponseDTO);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        LoginResponseDTO response = userService.refreshToken(refreshTokenDTO.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // Get User by Email
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        UserResponseDTO userResponseDTO = userService.getUserByEmail(email);
        return  ResponseEntity.status(HttpStatus.OK).body(userResponseDTO);
    }

    // Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponseDTO>> getUsersByRole(@PathVariable User.UserRole role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    // Search users by name or email (for messaging)
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) User.UserRole role) {
        
        if (query == null || query.trim().isEmpty()) {
            // If no query, return users by role or all users
            if (role != null) {
                return ResponseEntity.ok(userService.getUsersByRole(role));
            }
            return ResponseEntity.ok(userService.getAllUsers());
        }
        
        // Search by name or email
        List<UserResponseDTO> users = userService.searchUsers(query, role);
        return ResponseEntity.ok(users);
    }

    // Check if email exists
    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> emailExists(@PathVariable String email) {
        return ResponseEntity.ok(userService.isEmailExists(email));
    }

    // Get All Users
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getUsers() {
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    // Get User by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Integer id) {
        UserResponseDTO userResponseDTO = userService.getUserById(id);
        return  ResponseEntity.status(HttpStatus.OK).body(userResponseDTO);
    }

    // Update User
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Integer id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        UserResponseDTO updatedUser = userService.updateUser(id, userUpdateDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
    }

    // Update user status
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Integer id, @RequestParam User.UserStatus status) {
        userService.updateUserStatus(id, status);
        return ResponseEntity.ok().build();
    }

    // Update last seen
    @PatchMapping("/{id}/last-seen")
    public ResponseEntity<Void> updateLastSeen(@PathVariable Integer id) {
        userService.updateLastSeen(id);
        return ResponseEntity.ok().build();
    }

    // Delete User
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // Change password
    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(@PathVariable Integer id, @Valid @RequestBody PasswordChangeDTO passwordChangeDTO) {
        userService.changePassword(id, passwordChangeDTO);
        return ResponseEntity.ok().build();
    }

    // Activate user
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Integer id) {
        userService.activateUserAccount(id);
        return ResponseEntity.ok().build();
    }

    // Deactivate user
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Integer id) {
        userService.deactivateUserAccount(id);
        return ResponseEntity.ok().build();
    }
}
