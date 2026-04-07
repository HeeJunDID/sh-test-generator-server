package com.testcasegenerator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcasegenerator.domain.GenerationHistory;
import com.testcasegenerator.domain.GenerationHistoryRepository;
import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;
import com.testcasegenerator.infrastructure.ai.AiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final AiProvider aiProvider;
    private final GenerationHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<TestCaseDto> generate(GenerateRequest request) {
        log.info("Generating test cases for title={}, devCategory={}", request.getTitle(), request.getDevCategory());
        List<TestCaseDto> testCases = aiProvider.generateTestCases(request);
        log.info("Generated {} test cases for title={}", testCases.size(), request.getTitle());
        saveHistory(request, testCases);
        return testCases;
    }

    private void saveHistory(GenerateRequest request, List<TestCaseDto> testCases) {
        try {
            String json = objectMapper.writeValueAsString(testCases);
            GenerationHistory history = GenerationHistory.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .devCategory(request.getDevCategory())
                    .testCasesJson(json)
                    .testCaseCount(testCases.size())
                    .build();
            historyRepository.save(history);
        } catch (Exception e) {
            log.warn("Failed to save generation history for title={}", request.getTitle(), e);
        }
    }
}
