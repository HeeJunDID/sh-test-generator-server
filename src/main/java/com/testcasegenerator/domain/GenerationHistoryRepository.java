package com.testcasegenerator.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenerationHistoryRepository extends JpaRepository<GenerationHistory, Long> {

    List<GenerationHistory> findAllByOrderByCreatedAtDesc();
}
