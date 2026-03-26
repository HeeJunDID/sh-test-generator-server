package com.testcasegenerator.service;

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

    @Override
    public List<TestCaseDto> generate(GenerateRequest request) {
        log.info("Generating test cases for title={}, devCategory={}", request.getTitle(), request.getDevCategory());
        List<TestCaseDto> testCases = aiProvider.generateTestCases(request);
        log.info("Generated {} test cases for title={}", testCases.size(), request.getTitle());
        return testCases;
    }
}
