package com.testcasegenerator.controller;

import com.testcasegenerator.common.response.ApiResponse;
import com.testcasegenerator.dto.response.HistoryDetailDto;
import com.testcasegenerator.dto.response.HistoryListItemDto;
import com.testcasegenerator.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HistoryListItemDto>>> getHistoryList(
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok(historyService.getHistoryList(username)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HistoryDetailDto>> getHistoryDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok(historyService.getHistoryDetail(id, username)));
    }
}
