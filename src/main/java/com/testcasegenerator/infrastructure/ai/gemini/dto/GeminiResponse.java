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
    }

    public String extractText() {
        if (candidates == null || candidates.isEmpty()) {
            return "";
        }
        Content content = candidates.get(0).getContent();
        if (content == null || content.getParts() == null || content.getParts().isEmpty()) {
            return "";
        }
        return content.getParts().get(0).getText();
    }
}
