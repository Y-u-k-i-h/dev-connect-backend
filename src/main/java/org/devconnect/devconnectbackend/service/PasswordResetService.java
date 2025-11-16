package org.devconnect.devconnectbackend.service;

import org.devconnect.devconnectbackend.model.User;
import org.devconnect.devconnectbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PasswordResetService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

     // Step 1: Request reset code - sends 6-digit code to user's email
    public void requestResetCode(String email) {
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        if (user.isActive() != true) {
            throw new RuntimeException("User account is not active");
        }

        // Generate 6-digit code
        String resetCode = String.format("%06d", new Random().nextInt(999999));

        // Set code and expiry (15 minutes)
        user.setAuthCode(resetCode);
        user.setAuthCodeExpiry(LocalDateTime.now().plusMinutes(15));

        // Save user
        userRepository.save(user);

        // Send email
        emailService.sendPasswordRestEmail(email, resetCode);
    }


     // Step 2: Verify reset code - checks if code is valid
    public boolean verifyResetCode(String email, String code) {
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if code exists
        if (user.getAuthCode() == null) {
            throw new RuntimeException("No reset code found. Please request a new one.");
        }

        // Check if code matches
        if (!user.getAuthCode().equals(code)) {
            throw new RuntimeException("Invalid code");
        }

        // Check if code is expired
        if (user.getAuthCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code has expired. Please request a new one.");
        }

        return true;
    }

     // Step 3: Reset password - verifies code again and updates password
    public void resetPassword(String email, String newPassword, String confirmNewPassword) {

        if (!newPassword.equals(confirmNewPassword)) {
            throw new RuntimeException("New password and confirmation password do not match");
        }

        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));

        // Clear reset code
        user.setAuthCode(null);
        user.setAuthCodeExpiry(null);

        // Save user
        userRepository.save(user);
    }
}