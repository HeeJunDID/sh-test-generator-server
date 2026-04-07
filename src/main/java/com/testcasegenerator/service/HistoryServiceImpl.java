package com.testcasegenerator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcasegenerator.common.exception.BusinessException;
import com.testcasegenerator.domain.GenerationHistory;
import com.testcasegenerator.domain.GenerationHistoryRepository;
import com.testcasegenerator.dto.response.HistoryDetailDto;
import com.testcasegenerator.dto.response.HistoryListItemDto;
import com.testcasegenerator.dto.response.TestCaseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final GenerationHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<HistoryListItemDto> getHistoryList() {
        return historyRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(HistoryListItemDto::new)
                .toList();
    }

    @Override
    public HistoryDetailDto getHistoryDetail(Long id) {
        GenerationHistory history = historyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("이력을 찾을 수 없습니다. id=" + id));
        try {
            List<TestCaseDto> testCases = objectMapper.readValue(
                    history.getTestCasesJson(),
                    new TypeReference<>() {}
            );
            return new HistoryDetailDto(history, testCases);
        } catch (Exception e) {
            log.error("Failed to parse testCasesJson for history id={}", id, e);
            throw new BusinessException("이력 데이터를 불러오는 데 실패했습니다.");
        }
    }
}
