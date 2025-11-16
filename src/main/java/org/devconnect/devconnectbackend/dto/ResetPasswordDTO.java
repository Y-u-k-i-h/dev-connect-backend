package org.devconnect.devconnectbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordDTO {

    @NotBlank(message = "Email must not be blank")
    private String email;

    @NotBlank(message = "New password must not be blank")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    private String newPassword;

    @NotBlank(message = "Confirm password must not be blank")
    @Size(min = 8, message = "Confirm password must be at least 8 characters long")
    private String confirmPassword;
}
