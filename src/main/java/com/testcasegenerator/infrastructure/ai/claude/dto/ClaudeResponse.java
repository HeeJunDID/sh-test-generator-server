package com.testcasegenerator.infrastructure.ai.claude.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ClaudeResponse {

    private String id;
    private String type;
    private List<ContentBlock> content;
    private String model;

    @Getter
    @NoArgsConstructor
    public static class ContentBlock {
        private String type;
        private String text;
    }

    public String extractText() {
        if (content == null || content.isEmpty()) {
            return "";
        }
        return content.stream()
                .filter(block -> "text".equals(block.getType()))
                .map(ContentBlock::getText)
                .findFirst()
                .orElse("");
    }
}
