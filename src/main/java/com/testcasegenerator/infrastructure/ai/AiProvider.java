package com.testcasegenerator.infrastructure.ai;

import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;

import java.util.List;

/**
 * AI 제공자 추상화 인터페이스.
 * Claude, OpenAI 등 다양한 AI 제공자로 교체 가능하도록 설계.
 */
public interface AiProvider {

    List<TestCaseDto> generateTestCases(GenerateRequest request);
}
