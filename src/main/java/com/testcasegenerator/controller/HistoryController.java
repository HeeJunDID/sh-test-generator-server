package com.testcasegenerator.controller;

import com.testcasegenerator.common.response.ApiResponse;
import com.testcasegenerator.dto.response.HistoryDetailDto;
import com.testcasegenerator.dto.response.HistoryListItemDto;
import com.testcasegenerator.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    /**
     * 테스트케이스 생성 이력 목록 조회
     *
     * GET /api/history
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<HistoryListItemDto>>> getHistoryList() {
        return ResponseEntity.ok(ApiResponse.ok(historyService.getHistoryList()));
    }

    /**
     * 특정 이력의 테스트케이스 상세 조회
     *
     * GET /api/history/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HistoryDetailDto>> getHistoryDetail(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(historyService.getHistoryDetail(id)));
    }
}
