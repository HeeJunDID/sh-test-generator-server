package com.testcasegenerator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcasegenerator.domain.GenerationHistory;
import com.testcasegenerator.domain.GenerationHistoryRepository;
import com.testcasegenerator.domain.UserRepository;
import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;
import com.testcasegenerator.infrastructure.ai.AiProviderRouter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl implements TestCaseService {

    private final AiProviderRouter aiProviderRouter;
    private final GenerationHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<TestCaseDto> generate(GenerateRequest request) {
        String username = getUsername();
        String preferredProvider = userRepository.findByUsername(username)
                .map(u -> u.getPreferredAiProvider())
                .orElse(null);

        log.info("Generating test cases: title={}, user={}, provider={}", request.getTitle(), username, preferredProvider);
        List<TestCaseDto> testCases = aiProviderRouter.resolve(preferredProvider).generateTestCases(request);
        log.info("Generated {} test cases for title={}", testCases.size(), request.getTitle());
        saveHistory(username, request, testCases);
        return testCases;
    }

    private void saveHistory(String username, GenerateRequest request, List<TestCaseDto> testCases) {
        try {
            String json = objectMapper.writeValueAsString(testCases);
            historyRepository.save(GenerationHistory.builder()
                    .username(username)
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .devCategory(request.getDevCategory())
                    .testCasesJson(json)
                    .testCaseCount(testCases.size())
                    .build());
        } catch (Exception e) {
            log.warn("Failed to save generation history for title={}", request.getTitle(), e);
        }
    }

    private String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal != null ? principal.toString() : "unknown";
    }
}
