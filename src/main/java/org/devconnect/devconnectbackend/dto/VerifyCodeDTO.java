package org.devconnect.devconnectbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyCodeDTO {

    @NotBlank(message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "Verification code cannot be blank")
    @Length(min = 6, max = 6, message = "Verification code must be 6 characters long")
    private String verificationCode;
}
