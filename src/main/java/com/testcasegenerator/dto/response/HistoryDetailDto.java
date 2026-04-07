package com.testcasegenerator.dto.response;

import com.testcasegenerator.domain.GenerationHistory;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class HistoryDetailDto {
    private final Long id;
    private final String title;
    private final String description;
    private final String devCategory;
    private final int testCaseCount;
    private final LocalDateTime createdAt;
    private final List<TestCaseDto> testCases;

    public HistoryDetailDto(GenerationHistory h, List<TestCaseDto> testCases) {
        this.id = h.getId();
        this.title = h.getTitle();
        this.description = h.getDescription();
        this.devCategory = h.getDevCategory();
        this.testCaseCount = h.getTestCaseCount();
        this.createdAt = h.getCreatedAt();
        this.testCases = testCases;
    }
}
