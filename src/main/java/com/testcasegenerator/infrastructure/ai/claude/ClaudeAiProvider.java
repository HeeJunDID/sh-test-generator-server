package com.testcasegenerator.infrastructure.ai.claude;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcasegenerator.common.exception.BusinessException;
import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;
import com.testcasegenerator.infrastructure.ai.AiProvider;
import com.testcasegenerator.infrastructure.ai.claude.dto.ClaudeRequest;
import com.testcasegenerator.infrastructure.ai.claude.dto.ClaudeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "claude", matchIfMissing = true)
@RequiredArgsConstructor
public class ClaudeAiProvider implements AiProvider {

    private static final String SYSTEM_PROMPT = """
            당신은 소프트웨어 품질 보증(QA) 전문가입니다.
            요구사항을 분석하여 포괄적인 테스트케이스를 생성합니다.

            반드시 유효한 JSON 배열만 응답하세요. 설명, 마크다운 코드블록, 추가 텍스트 없이 JSON 배열만 출력하세요.

            각 테스트케이스 객체는 다음 필드를 가져야 합니다:
            - id: string (형식: "TC-001", "TC-002", ...)
            - programName: string (프로그램 식별자, 예: "SCR_LOGIN_01")
            - testData: string (테스트 데이터 설명)
            - title: string (테스트케이스 제목)
            - precondition: string (사전 조건)
            - steps: string[] (테스트 수행 단계 배열)
            - expected: string (기대 결과)
            - priority: string ("high", "medium", "low" 중 하나)
            - category: string ("신규기능", "수정기능", "예외처리", "성능", "경계값" 중 하나)
            """;

    private final RestClient anthropicRestClient;
    private final ObjectMapper objectMapper;

    @Value("${anthropic.model}")
    private String model;

    @Value("${anthropic.max-tokens}")
    private int maxTokens;

    @Override
    public List<TestCaseDto> generateTestCases(GenerateRequest request) {
        String userPrompt = buildUserPrompt(request);
        log.debug("Calling Claude API for test case generation. title={}", request.getTitle());

        ClaudeRequest claudeRequest = ClaudeRequest.builder()
                .model(model)
                .maxTokens(maxTokens)
                .system(SYSTEM_PROMPT)
                .messages(List.of(
                        ClaudeRequest.Message.builder()
                                .role("user")
                                .content(userPrompt)
                                .build()
                ))
                .build();

        ClaudeResponse response = anthropicRestClient.post()
                .uri("/v1/messages")
                .body(claudeRequest)
                .retrieve()
                .onStatus(status -> status.isError(), (req, res) -> {
                    log.error("Claude API error: status={}", res.getStatusCode());
                    throw new BusinessException("AI 서비스 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY);
                })
                .body(ClaudeResponse.class);

        if (response == null) {
            throw new BusinessException("AI 서비스 응답이 없습니다.", HttpStatus.BAD_GATEWAY);
        }

        String rawText = response.extractText();
        log.debug("Claude API raw response length={}", rawText.length());

        return parseTestCases(rawText);
    }

    private String buildUserPrompt(GenerateRequest req) {
        return String.format("""
                다음 요구사항에 대한 테스트케이스를 생성해주세요:

                제목: %s
                업무 내용: %s
                개발 구분: %s (%s)
                신규 여부: %s (%s)
                DB 작업 여부: %s (%s)
                금전 관련 여부: %s (%s)

                정상 케이스, 예외 케이스, 경계값 케이스를 포함한 6~10개의 테스트케이스를 JSON 배열로만 응답하세요.
                """,
                req.getTitle(),
                req.getDescription(),
                req.getDevCategory(), translateDevCategory(req.getDevCategory()),
                req.getIsNew(), translateIsNew(req.getIsNew()),
                req.getDbWork(), translateDbWork(req.getDbWork()),
                req.getMonetary(), translateMonetary(req.getMonetary())
        );
    }

    private List<TestCaseDto> parseTestCases(String rawText) {
        String json = extractJsonArray(rawText);
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse AI response as TestCaseDto list: {}", json, e);
            throw new BusinessException("AI 응답을 파싱하는데 실패했습니다.");
        }
    }

    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) {
            log.error("No JSON array found in response: {}", text);
            throw new BusinessException("AI 응답에서 JSON 배열을 찾을 수 없습니다.");
        }
        return text.substring(start, end + 1);
    }

    private String translateDevCategory(String value) {
        return switch (value) {
            case "screen" -> "화면";
            case "online" -> "온라인";
            case "batch" -> "배치";
            default -> value;
        };
    }

    private String translateIsNew(String value) {
        return switch (value) {
            case "new" -> "신규";
            case "existing" -> "기존 수정";
            default -> value;
        };
    }

    private String translateDbWork(String value) {
        return switch (value) {
            case "target" -> "DB 작업 있음";
            case "non-target" -> "DB 작업 없음";
            default -> value;
        };
    }

    private String translateMonetary(String value) {
        return switch (value) {
            case "yes" -> "금전 관련 있음";
            case "no" -> "금전 관련 없음";
            default -> value;
        };
    }
}
