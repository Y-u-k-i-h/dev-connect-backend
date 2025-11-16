package org.devconnect.devconnectbackend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq_gen")
    @SequenceGenerator(name = "user_seq_gen", sequenceName = "user_seq", allocationSize = 1)
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "first_name", nullable = false, length = 127)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 127)
    private String lastName;

    @Email
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "telephone", nullable = true, length = 15)
    private String telephone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 10)
    private UserRole userRole;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name="user_status", nullable = false)
    private UserStatus userStatus = UserStatus.OFFLINE;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "auth_code", length = 6)
    @Size(min = 6, max = 6)
    private String authCode;

    @Column(name = "auth_code_expiry")
    private LocalDateTime authCodeExpiry;

    public enum UserRole {
        CLIENT,
        DEVELOPER,
        ADMIN
    }

    public enum UserStatus {
        ONLINE,
        OFFLINE
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
