package com.testcasegenerator.infrastructure.ai.gemini.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class GeminiResponse {

    private List<Candidate> candidates;

    @Getter
    @NoArgsConstructor
    public static class Candidate {
        private Content content;
    }

    @Getter
    @NoArgsConstructor
    public static class Content {
        private List<Part> parts;
        private String role;
    }

    @Getter
    @NoArgsConstructor
    public static class Part {
        private String text;
        private Boolean thought;  // gemini-2.5-flash 등 thinking 모델의 사고 과정 part
    }

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }
        Content content = candidates.get(0).getContent();
        if (content == null || content.getParts() == null || content.getParts().isEmpty()) {
            return "";
        }
        // thought=true 인 part(thinking 내용)를 제외하고 실제 응답 텍스트만 반환
        return content.getParts().stream()
                .filter(part -> !Boolean.TRUE.equals(part.getThought()))
                .map(Part::getText)
                .findFirst()
                .orElse("");
    }
}
