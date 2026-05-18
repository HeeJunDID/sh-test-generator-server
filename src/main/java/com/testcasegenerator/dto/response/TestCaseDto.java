package com.testcasegenerator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseDto {

    private String id;           // TC-001, TC-002, ...
    private String programName;  // 프로그램 식별자 (e.g. SCR_LOGIN_01)
    private String testData;     // 테스트 데이터 설명
    private String title;        // 테스트케이스 제목
    private String precondition; // 사전조건
    private List<String> steps;  // 테스트 수행 단계
    private String expected;     // 기대 결과
    private String priority;     // high | medium | low
    private String category;     // 신규기능 | 수정기능 | 예외처리 | 성능 | 경계값
}
