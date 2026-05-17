package com.testcasegenerator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testcasegenerator.common.exception.BusinessException;
import com.testcasegenerator.domain.AdminTestCase;
import com.testcasegenerator.domain.AdminTestCaseRepository;
import com.testcasegenerator.dto.request.AdminTestCaseRequest;
import com.testcasegenerator.dto.response.AdminTestCaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTestCaseServiceImpl implements AdminTestCaseService {

    private final AdminTestCaseRepository adminTestCaseRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<AdminTestCaseResponse> getAll() {
        return adminTestCaseRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(e -> AdminTestCaseResponse.from(e, objectMapper))
                .toList();
    }

    @Override
    @Transactional
    public AdminTestCaseResponse create(AdminTestCaseRequest request, String createdBy) {
        AdminTestCase entity = buildEntity(request, createdBy);
        return AdminTestCaseResponse.from(adminTestCaseRepository.save(entity), objectMapper);
    }

    @Override
    @Transactional
    public void createBulk(List<AdminTestCaseRequest> requests, String createdBy) {
        List<AdminTestCase> entities = requests.stream()
                .map(r -> buildEntity(r, createdBy))
                .toList();
        adminTestCaseRepository.saveAll(entities);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!adminTestCaseRepository.existsById(id)) {
            throw new BusinessException("존재하지 않는 테스트케이스입니다.", HttpStatus.NOT_FOUND);
        }
        adminTestCaseRepository.deleteById(id);
    }

    private AdminTestCase buildEntity(AdminTestCaseRequest request, String createdBy) {
        String stepsJson = null;
        if (request.getSteps() != null && !request.getSteps().isEmpty()) {
            try {
                stepsJson = objectMapper.writeValueAsString(request.getSteps());
            } catch (Exception e) {
                log.warn("Failed to serialize steps");
            }
        }
        return AdminTestCase.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .programName(request.getProgramName())
                .testData(request.getTestData())
                .precondition(request.getPrecondition())
                .stepsJson(stepsJson)
                .expected(request.getExpected())
                .priority(request.getPriority())
                .createdBy(createdBy)
                .build();
    }
}
