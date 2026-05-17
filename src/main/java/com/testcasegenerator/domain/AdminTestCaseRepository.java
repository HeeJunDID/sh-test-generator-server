package com.testcasegenerator.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminTestCaseRepository extends JpaRepository<AdminTestCase, Long> {
    List<AdminTestCase> findAllByOrderByCreatedAtDesc();
}
