package org.devconnect.devconnectbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Developer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "developer_seq_gen")
    @SequenceGenerator(name = "developer_seq_gen", sequenceName = "developer_seq", allocationSize = 1)
    @Column(name = "developer_id")
    private Integer developerId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "bio", nullable = true, columnDefinition = "TEXT")
    private String bio;

    @Column(name = "skills", nullable = true, columnDefinition = "TEXT")
    private  String skills;

    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "github_url", nullable = true, length = 500)
    private String githubUrl;

    @Column(name = "linkedin_url", nullable = true, length = 500)
    private String linkedinUrl;

    @Column(name = "portfolio_url", nullable = true, length = 500)
    private String portfolioUrl;

    @Column(name = "avatar_url", nullable = true, length = 500)
    private  String avatarUrl;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column(name = "total_projects_completed")
    private Integer totalProjectsCompleted = 0;
}
