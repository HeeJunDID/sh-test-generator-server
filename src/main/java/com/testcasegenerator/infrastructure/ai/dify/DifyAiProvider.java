package com.testcasegenerator.infrastructure.ai.dify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcasegenerator.common.exception.BusinessException;
import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;
import com.testcasegenerator.infrastructure.ai.AiProvider;
import com.testcasegenerator.infrastructure.ai.dify.dto.DifyRequest;
import com.testcasegenerator.infrastructure.ai.dify.dto.DifyResponse;
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
    private final ObjectMapper objectMapper;

    @Value("${dify.api-key}")
    private String apiKey;

    @Override
    public List<TestCaseDto> generateTestCases(GenerateRequest request) {
        log.debug("Calling Dify workflow API for test case generation. title={}", request.getTitle());

        Map<String, Object> requirementsMap = new HashMap<>();
        requirementsMap.put("title", request.getTitle());
        requirementsMap.put("description", request.getDescription());
        requirementsMap.put("devType", translateDevCategory(request.getDevCategory()));
        requirementsMap.put("isNew", translateIsNew(request.getIsNew()));
        requirementsMap.put("isDbTask", translateDbWork(request.getDbWork()));
        requirementsMap.put("isFinancial", translateMonetary(request.getMonetary()));

        // Dify workflow expects jsonData as a JSON array string
        String jsonData;
        try {
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
                .header("Authorization", "Bearer " + apiKey)
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

        List<Map<String, Object>> rawCases = response.extractTestCases();
        log.debug("Dify workflow returned {} test cases", rawCases.size());

        return mapToTestCaseDtos(rawCases);
    }

    private List<TestCaseDto> mapToTestCaseDtos(List<Map<String, Object>> rawCases) {
        List<TestCaseDto> result = new ArrayList<>();
        for (Map<String, Object> raw : rawCases) {
            result.add(TestCaseDto.builder()
                    .id(toString(raw.get("testCaseId")))
                    .programName(toString(raw.get("programName")))
                    .testData(toString(raw.get("testData")))
                    .title(toString(raw.get("testCaseName")))
                    .precondition(toString(raw.get("precondition")))
                    .steps(extractSteps(raw.get("steps")))
                    .expected(toString(raw.get("expectedResult")))
                    .priority(normalizePriority(toString(raw.get("priority"))))
                    .category(translateCategory(toString(raw.get("type"))))
                    .build());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractSteps(Object stepsObj) {
        if (stepsObj instanceof List<?> list) {
            return (List<String>) list;
        }
        if (stepsObj instanceof String s && !s.isBlank()) {
            return List.of(s);
        }
        return List.of();
    }

    private String toString(Object obj) {
        return obj != null ? obj.toString() : "";
    }

    private String normalizePriority(String priority) {
        if (priority == null) return "medium";
        return priority.toLowerCase();
    }

    private String translateCategory(String type) {
        if (type == null) return "";
        return switch (type) {
            case "Positive", "신규기능", "new_feature" -> "신규기능";
            case "Modified", "수정기능", "modified_feature" -> "수정기능";
            case "Negative", "예외처리", "exception" -> "예외처리";
            case "Performance", "성능", "performance" -> "성능";
            case "Boundary", "경계값", "boundary" -> "경계값";
            default -> type;
        };
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
