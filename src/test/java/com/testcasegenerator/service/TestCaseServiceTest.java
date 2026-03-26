package com.testcasegenerator.service;

import com.testcasegenerator.common.exception.BusinessException;
import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;
import com.testcasegenerator.infrastructure.ai.AiProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TestCaseServiceTest {

    @InjectMocks
    private TestCaseServiceImpl testCaseService;

    @Mock
    private AiProvider aiProvider;

    @Test
    @DisplayName("generate - AI Provider 호출 결과를 그대로 반환")
    void generate_returns_ai_provider_result() {
        // given
        GenerateRequest request = createRequest();
        List<TestCaseDto> expected = List.of(
                TestCaseDto.builder().id("TC-001").title("정상 케이스").priority("high").build(),
                TestCaseDto.builder().id("TC-002").title("예외 케이스").priority("medium").build()
        );
        given(aiProvider.generateTestCases(any(GenerateRequest.class))).willReturn(expected);

        // when
        List<TestCaseDto> result = testCaseService.generate(request);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("TC-001");
        assertThat(result.get(1).getId()).isEqualTo("TC-002");
        verify(aiProvider).generateTestCases(request);
    }

    @Test
    @DisplayName("generate - AI Provider 예외 발생 시 그대로 전파")
    void generate_propagates_ai_provider_exception() {
        // given
        GenerateRequest request = createRequest();
        given(aiProvider.generateTestCases(any(GenerateRequest.class)))
                .willThrow(new BusinessException("AI 서비스 호출에 실패했습니다."));

        // when & then
        assertThatThrownBy(() -> testCaseService.generate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI 서비스 호출에 실패했습니다.");
    }

    @Test
    @DisplayName("generate - AI Provider가 빈 리스트 반환 시 빈 리스트 반환")
    void generate_returns_empty_list_when_ai_returns_empty() {
        // given
        GenerateRequest request = createRequest();
        given(aiProvider.generateTestCases(any(GenerateRequest.class))).willReturn(List.of());

        // when
        List<TestCaseDto> result = testCaseService.generate(request);

        // then
        assertThat(result).isEmpty();
    }

    private GenerateRequest createRequest() {
        try {
            var field = GenerateRequest.class.getDeclaredField("title");
            // Reflection 대신 ObjectMapper 사용
        } catch (NoSuchFieldException e) {
            // ignore
        }

        // 테스트용 인스턴스 생성 (Jackson deserialization 방식)
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        try {
            return mapper.readValue("""
                    {
                      "title": "회원 로그인 기능",
                      "description": "이메일과 비밀번호로 로그인",
                      "devCategory": "screen",
                      "isNew": "new",
                      "dbWork": "target",
                      "monetary": "no"
                    }
                    """, GenerateRequest.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
