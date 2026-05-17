package com.testcasegenerator.service;

import com.testcasegenerator.dto.response.HistoryDetailDto;
import com.testcasegenerator.dto.response.HistoryListItemDto;

import java.util.List;

public interface HistoryService {

    List<HistoryListItemDto> getHistoryList(String username);

    HistoryDetailDto getHistoryDetail(Long id, String username);
}
