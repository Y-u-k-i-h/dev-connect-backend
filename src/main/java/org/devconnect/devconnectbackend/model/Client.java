package org.devconnect.devconnectbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "client_sequence")
    @SequenceGenerator(name = "client_sequence", sequenceName = "client_sequence", allocationSize = 1)
    @Column(name = "client_id")
    private Integer clientId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "company_name", length = 255)
    private String companyName;

    @Column(length = 127)
    private String industry;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 255)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 511)
    private String avatarUrl;
}
