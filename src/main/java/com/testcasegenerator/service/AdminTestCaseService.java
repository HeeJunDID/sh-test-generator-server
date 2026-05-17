package com.testcasegenerator.service;

import com.testcasegenerator.dto.request.AdminTestCaseRequest;
import com.testcasegenerator.dto.response.AdminTestCaseResponse;

import java.util.List;

public interface AdminTestCaseService {
    List<AdminTestCaseResponse> getAll();
    AdminTestCaseResponse create(AdminTestCaseRequest request, String createdBy);
    void createBulk(List<AdminTestCaseRequest> requests, String createdBy);
    void delete(Long id);
}
