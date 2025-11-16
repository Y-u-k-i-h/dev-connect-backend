package org.devconnect.devconnectbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
}
