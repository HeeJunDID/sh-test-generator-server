package com.testcasegenerator.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_test_cases")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String category;
    private String programName;
    private String testData;

    @Column(length = 2000)
    private String precondition;

    @Column(columnDefinition = "CLOB")
    private String stepsJson;

    @Column(length = 2000)
    private String expected;

    private String priority;
    private String createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
