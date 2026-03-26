package com.testcasegenerator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;
import com.testcasegenerator.service.TestCaseService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestCaseController.class)
class TestCaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TestCaseService testCaseService;

    @Test
    @DisplayName("POST /api/generate - 정상 요청 시 200과 테스트케이스 목록 반환")
    void generate_success() throws Exception {
        // given
        GenerateRequest request = validRequest();
        List<TestCaseDto> mockResult = List.of(
                TestCaseDto.builder()
                        .id("TC-001")
                        .programName("SCR_LOGIN_01")
                        .title("정상 로그인 테스트")
                        .precondition("사용자가 가입되어 있음")
                        .steps(List.of("1. 아이디 입력", "2. 비밀번호 입력", "3. 로그인 버튼 클릭"))
                        .expected("메인 화면으로 이동")
                        .priority("high")
                        .category("신규기능")
                        .build()
        );
        given(testCaseService.generate(any(GenerateRequest.class))).willReturn(mockResult);

        // when & then
        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("TC-001"))
                .andExpect(jsonPath("$.data[0].title").value("정상 로그인 테스트"))
                .andExpect(jsonPath("$.data[0].priority").value("high"));
    }

    @Test
    @DisplayName("POST /api/generate - title 누락 시 400 반환")
    void generate_missing_title_returns_400() throws Exception {
        // given
        String requestJson = """
                {
                  "description": "로그인 기능",
                  "devCategory": "screen",
                  "isNew": "new",
                  "dbWork": "target",
                  "monetary": "no"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/generate - description 누락 시 400 반환")
    void generate_missing_description_returns_400() throws Exception {
        // given
        String requestJson = """
                {
                  "title": "로그인 기능",
                  "devCategory": "screen",
                  "isNew": "new",
                  "dbWork": "target",
                  "monetary": "no"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/generate - 빈 요청 바디 시 400 반환")
    void generate_empty_body_returns_400() throws Exception {
        mockMvc.perform(post("/api/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private GenerateRequest validRequest() throws Exception {
        String json = """
                {
                  "title": "회원 로그인 기능",
                  "description": "이메일과 비밀번호로 로그인하는 기능",
                  "devCategory": "screen",
                  "isNew": "new",
                  "dbWork": "target",
                  "monetary": "no"
                }
                """;
        return objectMapper.readValue(json, GenerateRequest.class);
    }
}
