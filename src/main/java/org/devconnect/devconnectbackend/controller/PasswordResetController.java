package org.devconnect.devconnectbackend.controller;

import jakarta.validation.Valid;
import org.apache.coyote.http11.filters.SavedRequestInputFilter;
import org.devconnect.devconnectbackend.dto.ForgotPasswordDTO;
import org.devconnect.devconnectbackend.dto.ResetPasswordDTO;
import org.devconnect.devconnectbackend.dto.VerifyCodeDTO;
import org.devconnect.devconnectbackend.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/request-code")
     public ResponseEntity<String> requestResetCode(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        passwordResetService.requestResetCode(forgotPasswordDTO.getEmail());
        return ResponseEntity.ok("Password reset code sent to email if it exists.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyResetCode(@Valid @RequestBody VerifyCodeDTO verifyCodeDTO) {
        passwordResetService.verifyResetCode(verifyCodeDTO.getEmail(), verifyCodeDTO.getVerificationCode());
        return ResponseEntity.ok("Verification code is valid. You may proceed to reset your password.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        passwordResetService.resetPassword(resetPasswordDTO.getEmail(), resetPasswordDTO.getNewPassword(), resetPasswordDTO.getConfirmPassword());
        return ResponseEntity.ok("Password has been successfully reset.");
    }
}
