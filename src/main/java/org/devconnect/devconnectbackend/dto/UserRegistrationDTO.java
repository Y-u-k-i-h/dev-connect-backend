package org.devconnect.devconnectbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.devconnect.devconnectbackend.model.User;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDTO {

    @NotBlank(message = "First name is required")
    @Size(max = 127, message = "First name must not exceed 127 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 127, message = "Last name must not exceed 127 characters")
    private String lastName;

    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 15, message = "Telephone must not exceed 15 characters")
    private String telephone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @NotNull(message = "User role is required")
    private User.UserRole userRole;

}