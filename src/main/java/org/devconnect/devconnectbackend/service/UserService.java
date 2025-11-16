package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.dto.*;
import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.devconnect.devconnectbackend.utills.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public UserResponseDTO registerUser(UserRegistrationDTO userRegistrationDTO) {
        // Check if email already exists
        if (userRepository.existsByEmail(userRegistrationDTO.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Map DTO to User entity
        User user = userMapper.toUserModel(userRegistrationDTO);

        // Hash the password before saving
        user.setPasswordHash(passwordEncoder.encode(userRegistrationDTO.getPassword()));

        // Save user to the database
        User savedUser = userRepository.save(user);

        // Convert saved Entity to responseDTO
        UserResponseDTO responseDTO = userMapper.toUserResponseDTO(savedUser);

        return responseDTO;
    }

    public UserResponseDTO getUserById(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponseDTO(user);
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponseDTO(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> userMapper.toUserResponseDTO(user))
                .collect(Collectors.toList());
    }

    public UserResponseDTO updateUser(Integer userId, UserUpdateDTO userUpdateDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        userMapper.updateUserFromDTO(userUpdateDTO, user);

        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponseDTO(updatedUser);
    }

    public void deleteUser(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);

        userRepository.save(user);
    }

    public LoginResponseDTO login(LoginDTO loginDTO) {
        User user = userRepository.findByEmail(loginDTO.getEmail()).orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getUserId());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail(), user.getUserId());

        UserResponseDTO userResponseDTO = userMapper.toUserResponseDTO(user);

        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();

        return new LoginResponseDTO(accessToken, refreshToken, userResponseDTO, "Bearer");
    }

    public void changePassword(Integer userId, PasswordChangeDTO passwordChangeDTO) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(passwordChangeDTO.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (passwordChangeDTO.getNewPassword().equals(passwordChangeDTO.getConfirmNewPassword())) {
            throw new RuntimeException("Passwords do not match");
        } else {
            user.setPasswordHash(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
            userRepository.save(user);
        }
    }

    public void updateUserStatus(Integer userId, User.UserStatus status) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        user.setUserStatus(status);

        userRepository.save(user);

        // TODO: Broadcast status change to all users via WebSocket
    }

    public String getUserStatus(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return user.getUserStatus().name().toLowerCase();
    }

    public void updateLastSeen(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        user.setLastSeen(LocalDateTime.now());

        userRepository.save(user);
    }

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void deactivateUserAccount(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);

        userRepository.save(user);
    }

    public void activateUserAccount(Integer userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(true);

        userRepository.save(user);
    }

    public List<UserResponseDTO> getUsersByRole(User.UserRole role) {
        List<User> users = userRepository.findByUserRole(role);

        return users.stream()
                .map(user -> userMapper.toUserResponseDTO(user))
                .collect(Collectors.toList());
    }

    public LoginResponseDTO refreshToken(String refreshToken) {
        // Validate the refresh token
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Extract user details from the token
        String email = jwtService.extractEmail(refreshToken);
        Integer userId = jwtService.extractUserId(refreshToken);

        // Verify user exists and is active
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.isActive()) {
            throw new RuntimeException("User account is deactivated");
        }

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(email, userId);
        String newRefreshToken = jwtService.generateRefreshToken(email, userId);

        // Convert user to DTO
        UserResponseDTO userResponseDTO = userMapper.toUserResponseDTO(user);
        return new LoginResponseDTO(newAccessToken, newRefreshToken, userResponseDTO, "Bearer");
    }
































     // TODO: ISSA WILL HAVE A MESSAGE SERVICE TO HANDLE METHODS RELATED TO MESSAGING BETWEEN USERS
//    @Transactional
//    public void updateUserStatus(Long userId, User.UserStatus status) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        user.setStatus(status);
//        if (status == User.UserStatus.OFFLINE) {
//            user.setLastSeen(LocalDateTime.now());
//        }
//        userRepository.save(user);
//
//        // Broadcast status change to all users
//        UserStatusDTO statusDTO = new UserStatusDTO(userId, status.name().toLowerCase());
//        messagingTemplate.convertAndSend("/topic/user-status", statusDTO);
//    }


}
