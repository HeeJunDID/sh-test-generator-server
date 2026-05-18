package com.testcasegenerator.infrastructure.ai.dify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcasegenerator.common.exception.BusinessException;
import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;
import com.testcasegenerator.infrastructure.ai.AiProvider;
import com.testcasegenerator.infrastructure.ai.dify.dto.DifyRequest;
import com.testcasegenerator.infrastructure.ai.dify.dto.DifyResponse;
import com.testcasegenerator.infrastructure.ai.gemini.dto.GeminiRequest;
import com.testcasegenerator.infrastructure.ai.gemini.dto.GeminiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("dify")
@RequiredArgsConstructor
public class DifyAiProvider implements AiProvider {

    @Qualifier("difyRestClient")
    private final RestClient difyRestClient;

    @Qualifier("geminiRestClient")
    private final RestClient geminiRestClient;

    private final ObjectMapper objectMapper;

    @Value("${dify.api-key}")
    private String difyApiKey;

    @Value("${gemini.api-key}")
    private String geminiApiKey;

    @Value("${gemini.model}")
    private String geminiModel;

    @Value("${gemini.max-tokens}")
    private int geminiMaxTokens;

    @Override
    public List<TestCaseDto> generateTestCases(GenerateRequest request) {
        log.debug("Calling Dify workflow API. title={}", request.getTitle());

        List<TestCaseDto> basicCases = callDify(request);
        log.debug("Dify returned {} basic test cases", basicCases.size());

        // List<TestCaseDto> enriched = enrichWithGemini(basicCases, request);
        // log.debug("Gemini enrichment complete. cases={}", enriched.size());
        // return enriched;

        return basicCases;
    }

    // ── Dify 호출 ──────────────────────────────────────────────────────────

    private List<TestCaseDto> callDify(GenerateRequest request) {
        Map<String, Object> requirementsMap = new HashMap<>();
        requirementsMap.put("title", request.getTitle());
        requirementsMap.put("description", request.getDescription());
        requirementsMap.put("devType", translateDevCategory(request.getDevCategory()));
        requirementsMap.put("isNew", translateIsNew(request.getIsNew()));
        requirementsMap.put("isDbTask", translateDbWork(request.getDbWork()));
        requirementsMap.put("isFinancial", translateMonetary(request.getMonetary()));

        String jsonData;
        try {
            // Dify 워크플로우는 jsonData를 JSON 배열 문자열로 받음
            jsonData = objectMapper.writeValueAsString(List.of(requirementsMap));
        } catch (Exception e) {
            throw new BusinessException("요청 데이터 직렬화에 실패했습니다.");
        }

        Map<String, Object> inputs = new HashMap<>();
        inputs.put("addFileYn", "N");
        inputs.put("jsonData", jsonData);

        DifyRequest difyRequest = DifyRequest.builder()
                .inputs(inputs)
                .responseMode("blocking")
                .user("testcase-generator")
                .build();

        DifyResponse response = difyRestClient.post()
                .uri("/v1/workflows/run")
                .header("Authorization", "Bearer " + difyApiKey)
                .body(difyRequest)
                .retrieve()
                .onStatus(status -> status.isError(), (req, res) -> {
                    String body = new String(res.getBody().readAllBytes());
                    log.error("Dify API error: status={}, body={}", res.getStatusCode(), body);
                    throw new BusinessException("AI 서비스 호출에 실패했습니다.", HttpStatus.BAD_GATEWAY);
                })
                .body(DifyResponse.class);

        if (response == null) {
            throw new BusinessException("AI 서비스 응답이 없습니다.", HttpStatus.BAD_GATEWAY);
        }
        if (!"succeeded".equals(response.getStatus())) {
            log.error("Dify workflow failed: status={}, error={}", response.getStatus(), response.getError());
            throw new BusinessException("AI 워크플로우 실행에 실패했습니다.", HttpStatus.BAD_GATEWAY);
        }

        return mapToTestCaseDtos(response.extractTestCases());
    }

    // ── Gemini 보강 ────────────────────────────────────────────────────────

    private List<TestCaseDto> enrichWithGemini(List<TestCaseDto> basicCases, GenerateRequest request) {
        String casesJson;
        try {
            casesJson = objectMapper.writeValueAsString(basicCases);
        } catch (Exception e) {
            log.warn("Failed to serialize basic cases for Gemini enrichment");
            return basicCases;
        }

        String prompt = String.format("""
                다음은 요구사항에 대해 생성된 테스트케이스 목록입니다.

                요구사항 제목: %s
                요구사항 내용: %s

                각 테스트케이스의 "testDetail" 필드에는 워크플로우가 생성한 테스트 시나리오 설명이 담겨 있습니다.
                이를 참고하여 아래 JSON 배열의 각 테스트케이스에 다음 세 필드를 추가해주세요:
                - "precondition": 사전 조건 (string)
                - "steps": 수행 단계 (string 배열, 3~7개)
                - "expected": 기대 결과 (string)

                기존 필드는 반드시 그대로 유지하고, 세 필드만 추가하세요.
                마크다운 코드블록 없이 JSON 배열만 응답하세요.

                테스트케이스:
                %s
                """, request.getTitle(), request.getDescription(), casesJson);

        GeminiRequest geminiRequest = GeminiRequest.builder()
                .contents(List.of(GeminiRequest.Content.builder()
                        .role("user")
                        .parts(List.of(GeminiRequest.Part.builder().text(prompt).build()))
                        .build()))
                .generationConfig(GeminiRequest.GenerationConfig.builder()
                        .maxOutputTokens(geminiMaxTokens)
                        .thinkingConfig(GeminiRequest.ThinkingConfig.builder()
                                .thinkingBudget(0)
                                .build())
                        .build())
                .build();

        try {
            GeminiResponse geminiResponse = geminiRestClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={apiKey}", geminiModel, geminiApiKey)
                    .body(geminiRequest)
                    .retrieve()
                    .onStatus(status -> status.isError(), (req, res) -> {
                        log.warn("Gemini enrichment error: {}", res.getStatusCode());
                        throw new RuntimeException("Gemini enrichment failed");
                    })
                    .body(GeminiResponse.class);

            if (geminiResponse == null) return basicCases;

            String rawText = geminiResponse.extractText();
            String json = extractJsonArray(rawText);
            List<TestCaseDto> enriched = objectMapper.readValue(json, new TypeReference<>() {});
            log.debug("Gemini enrichment succeeded. cases={}", enriched.size());
            return enriched;

        } catch (Exception e) {
            log.warn("Gemini enrichment failed, returning basic test cases. error={}", e.getMessage());
            return basicCases;
        }
    }

    // ── 공통 유틸 ──────────────────────────────────────────────────────────

    private List<TestCaseDto> mapToTestCaseDtos(List<Map<String, Object>> rawCases) {
        List<TestCaseDto> result = new ArrayList<>();
        for (Map<String, Object> raw : rawCases) {
            result.add(TestCaseDto.builder()
                    .id(toString(raw.get("testCaseId")))
                    .programName(toString(raw.get("programName")))
                    .testData(toString(raw.get("testData")))
                    .title(toString(raw.get("testCaseName")))
                    .precondition(toString(raw.get("precondition")))
                    .steps(parseTestSteps(toString(raw.get("testSteps"))))
                    .expected(toString(raw.get("expectedResult")))
                    .priority(normalizePriority(toString(raw.get("priority"))))
                    .category(translateCategory(toString(raw.get("type"))))
                    .build());
        }
        return result;
    }

    private List<String> parseTestSteps(String testSteps) {
        if (testSteps == null || testSteps.isBlank()) return List.of();
        // "1. 단계1. 2. 단계2. 3. 단계3." 형식 파싱
        String[] parts = testSteps.split("\\s*\\d+\\.\\s+");
        return java.util.Arrays.stream(parts)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private String extractJsonArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start == -1 || end == -1 || end <= start) {
            log.error("No JSON array in Gemini response: {}", text);
            throw new RuntimeException("No JSON array found");
        }
        return text.substring(start, end + 1);
    }

    private String toString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private String normalizePriority(String priority) {
        if (priority == null || priority.isBlank()) return "medium";
        return switch (priority.toLowerCase()) {
            case "high", "높음", "p1" -> "high";
            case "low", "낮음", "p3" -> "low";
            default -> "medium";
        };
    }

    private String translateCategory(String type) {
        if (type == null) return "";
        String lower = type.toLowerCase();
        if (lower.contains("boundary") || lower.contains("경계값")) return "경계값";
        if (lower.contains("performance") || lower.contains("성능")) return "성능";
        if (lower.contains("security") || lower.contains("보안")) return "보안";
        if (lower.contains("reliability") || lower.contains("신뢰")) return "신뢰성";
        if (lower.contains("usability") || lower.contains("사용성")) return "사용성";
        if (lower.contains("non-functional") || lower.contains("비기능")) return "비기능";
        if (lower.contains("negative") || lower.contains("예외")) return "예외";
        if (lower.contains("modified") || lower.contains("수정")) return "수정";
        if (lower.contains("positive") || lower.contains("functional") || lower.contains("기능")) return "기능";
        return type;
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
            case "existing" -> "기존수정";
            default -> value;
        };
    }

    private String translateDbWork(String value) {
        return switch (value) {
            case "target" -> "대상";
            case "non-target" -> "비대상";
            default -> value;
        };
    }

    private String translateMonetary(String value) {
        return switch (value) {
            case "yes" -> "대상";
            case "no" -> "비대상";
            default -> value;
        };
    }
}
