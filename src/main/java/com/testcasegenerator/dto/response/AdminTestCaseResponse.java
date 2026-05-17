package com.testcasegenerator.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcasegenerator.domain.AdminTestCase;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdminTestCaseResponse {

    private Long id;
    private String title;
    private String category;
    private String programName;
    private String testData;
    private String precondition;
    private List<String> steps;
    private String expected;
    private String priority;
    private String createdBy;
    private LocalDateTime createdAt;

    public static AdminTestCaseResponse from(AdminTestCase entity, ObjectMapper objectMapper) {
        List<String> steps = List.of();
        if (entity.getStepsJson() != null && !entity.getStepsJson().isBlank()) {
            try {
                steps = objectMapper.readValue(entity.getStepsJson(), new TypeReference<>() {});
            } catch (Exception ignored) {}
        }
        return AdminTestCaseResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .category(entity.getCategory())
                .programName(entity.getProgramName())
                .testData(entity.getTestData())
                .precondition(entity.getPrecondition())
                .steps(steps)
                .expected(entity.getExpected())
                .priority(entity.getPriority())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
