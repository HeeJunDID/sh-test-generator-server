package com.testcasegenerator.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_users")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String displayName;

    @Builder.Default
    private String preferredAiProvider = "dify";

    @Builder.Default
    private String role = "USER";

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public void updateSettings(String preferredAiProvider) {
        if (preferredAiProvider != null) {
            this.preferredAiProvider = preferredAiProvider;
        }
    }
}
