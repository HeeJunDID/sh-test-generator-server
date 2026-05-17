package com.testcasegenerator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class AdminTestCaseRequest {

    @NotBlank(message = "테스트케이스명은 필수입니다.")
    private String title;

    private String category;
    private String programName;
    private String testData;
    private String precondition;
    private List<String> steps;
    private String expected;
    private String priority;
}
