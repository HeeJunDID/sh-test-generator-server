package com.testcasegenerator.service;

import com.testcasegenerator.dto.request.GenerateRequest;
import com.testcasegenerator.dto.response.TestCaseDto;

import java.util.List;

public interface TestCaseService {

    List<TestCaseDto> generate(GenerateRequest request);
}
