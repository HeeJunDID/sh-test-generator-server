package com.testcasegenerator.service;

import com.testcasegenerator.dto.response.HistoryDetailDto;
import com.testcasegenerator.dto.response.HistoryListItemDto;

import java.util.List;

public interface HistoryService {

    List<HistoryListItemDto> getHistoryList();

    HistoryDetailDto getHistoryDetail(Long id);
}
