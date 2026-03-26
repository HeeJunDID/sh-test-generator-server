package com.testcasegenerator.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GenerateRequest {

    @NotBlank(message = "요구사항 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "업무 내용은 필수입니다.")
    private String description;

    @NotBlank(message = "개발 구분은 필수입니다.")
    private String devCategory;   // screen | online | batch

    @NotBlank(message = "신규 여부는 필수입니다.")
    private String isNew;         // new | existing

    @NotBlank(message = "DB 작업 여부는 필수입니다.")
    private String dbWork;        // target | non-target

    @NotBlank(message = "금전 여부는 필수입니다.")
    private String monetary;      // yes | no
}
