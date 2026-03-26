package com.testcasegenerator.infrastructure.ai.claude;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcasegenerator.common.exception.BusinessException;
import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;
import com.testcasegenerator.infrastructure.ai.claude.dto.ClaudeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ClaudeAiProviderTest {

    @Mock
    private RestClient anthropicRestClient;

    private ClaudeAiProvider claudeAiProvider;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        claudeAiProvider = new ClaudeAiProvider(anthropicRestClient, objectMapper);
        ReflectionTestUtils.setField(claudeAiProvider, "model", "claude-opus-4-6");
        ReflectionTestUtils.setField(claudeAiProvider, "maxTokens", 4096);
    }

    @Test
    @DisplayName("generateTestCases - 정상 JSON 응답 파싱 성공")
    void generateTestCases_parses_valid_json_response() throws Exception {
        // given
        String validJson = """
                [
                  {
                    "id": "TC-001",
                    "programName": "SCR_LOGIN_01",
                    "testData": "유효한 이메일과 비밀번호",
                    "title": "정상 로그인",
                    "precondition": "사용자가 가입되어 있음",
                    "steps": ["1. 이메일 입력", "2. 비밀번호 입력", "3. 로그인 클릭"],
                    "expected": "메인 화면 이동",
                    "priority": "high",
                    "category": "신규기능"
                  }
                ]
                """;

        stubClaudeResponse(validJson);

        // when
        List<TestCaseDto> result = claudeAiProvider.generateTestCases(createRequest());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("TC-001");
        assertThat(result.get(0).getPriority()).isEqualTo("high");
        assertThat(result.get(0).getSteps()).hasSize(3);
    }

    @Test
    @DisplayName("generateTestCases - 마크다운 코드블록으로 감싸진 JSON도 파싱 성공")
    void generateTestCases_parses_json_wrapped_in_markdown() throws Exception {
        // given
        String wrappedJson = """
                ```json
                [
                  {
                    "id": "TC-001",
                    "programName": "SCR_LOGIN_01",
                    "testData": "유효한 데이터",
                    "title": "정상 케이스",
                    "precondition": "조건",
                    "steps": ["1단계"],
                    "expected": "성공",
                    "priority": "high",
                    "category": "신규기능"
                  }
                ]
                ```
                """;

        stubClaudeResponse(wrappedJson);

        // when
        List<TestCaseDto> result = claudeAiProvider.generateTestCases(createRequest());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("TC-001");
    }

    @Test
    @DisplayName("generateTestCases - JSON 배열 없는 응답 시 BusinessException 발생")
    void generateTestCases_throws_when_no_json_array_in_response() throws Exception {
        // given
        stubClaudeResponse("안녕하세요. 테스트케이스를 생성해드리겠습니다.");

        // when & then
        assertThatThrownBy(() -> claudeAiProvider.generateTestCases(createRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("JSON 배열을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("generateTestCases - 잘못된 JSON 구조 시 BusinessException 발생")
    void generateTestCases_throws_when_invalid_json_structure() throws Exception {
        // given
        stubClaudeResponse("[{invalid json}]");

        // when & then
        assertThatThrownBy(() -> claudeAiProvider.generateTestCases(createRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("파싱");
    }

    // ---- helpers ----

    @SuppressWarnings("unchecked")
    private void stubClaudeResponse(String text) {
        ClaudeResponse mockResponse = mock(ClaudeResponse.class);
        given(mockResponse.extractText()).willReturn(text);

        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        given(anthropicRestClient.post()).willReturn(uriSpec);
        given(uriSpec.uri(any(String.class))).willReturn(bodySpec);
        given(bodySpec.body(any(Object.class))).willReturn(bodySpec);
        given(bodySpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.onStatus(any(), any())).willReturn(responseSpec);
        given(responseSpec.body(ClaudeResponse.class)).willReturn(mockResponse);
    }

    private GenerateRequest createRequest() throws Exception {
        return objectMapper.readValue("""
                {
                  "title": "회원 로그인",
                  "description": "이메일/비밀번호 로그인",
                  "devCategory": "screen",
                  "isNew": "new",
                  "dbWork": "target",
                  "monetary": "no"
                }
                """, GenerateRequest.class);
    }
}
