package org.devconnect.devconnectbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.devconnect.devconnectbackend.model.User;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperResponseDTO {
    private Integer developerId;
    private Integer userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String skills;
    private BigDecimal hourlyRate;
    private String githubUrl;
    private String linkedinUrl;
    private String portfolioUrl;
    private BigDecimal averageRating;
    private Integer totalProjectsCompleted;
    private User.UserStatus userStatus;
}
