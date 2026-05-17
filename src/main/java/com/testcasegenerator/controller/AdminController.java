package com.testcasegenerator.controller;

import com.testcasegenerator.common.response.ApiResponse;
import com.testcasegenerator.dto.request.AdminTestCaseRequest;
import com.testcasegenerator.dto.response.AdminTestCaseResponse;
import com.testcasegenerator.service.AdminTestCaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminTestCaseService adminTestCaseService;

    @GetMapping("/testcases")
    public ResponseEntity<ApiResponse<List<AdminTestCaseResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(adminTestCaseService.getAll()));
    }

    @PostMapping("/testcases")
    public ResponseEntity<ApiResponse<AdminTestCaseResponse>> create(@Valid @RequestBody AdminTestCaseRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(ApiResponse.ok(adminTestCaseService.create(request, username)));
    }

    @PostMapping("/testcases/bulk")
    public ResponseEntity<ApiResponse<Void>> createBulk(@RequestBody List<@Valid AdminTestCaseRequest> requests) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        adminTestCaseService.createBulk(requests, username);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/testcases/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        adminTestCaseService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
