package com.testcasegenerator.controller;

import com.testcasegenerator.common.response.ApiResponse;
import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;
import com.testcasegenerator.service.TestCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseService testCaseService;

    /**
     * 요구사항을 받아 테스트케이스를 생성합니다.
     *
     * POST /api/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<List<TestCaseDto>>> generate(
            @Valid @RequestBody GenerateRequest request
    ) {
        List<TestCaseDto> testCases = testCaseService.generate(request);
        return ResponseEntity.ok(ApiResponse.ok(testCases));
    }
}
