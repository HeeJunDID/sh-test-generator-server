package com.testcasegenerator.infrastructure.ai.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GeminiRequest {

    @JsonProperty("system_instruction")
    private SystemInstruction systemInstruction;

    private List<Content> contents;

    @JsonProperty("generationConfig")
    private GenerationConfig generationConfig;

    @Getter
    @Builder
    public static class SystemInstruction {
        private List<Part> parts;
    }

    @Getter
    @Builder
    public static class Content {
        private String role;
        private List<Part> parts;
    }

    @Getter
    @Builder
    public static class Part {
        private String text;
    }

    @Getter
    @Builder
    public static class GenerationConfig {
        @JsonProperty("maxOutputTokens")
        private int maxOutputTokens;

        // thinking 모델(gemini-2.5-flash 등)에서 thinking 비활성화 → 출력 토큰 확보
        @JsonProperty("thinkingConfig")
        private ThinkingConfig thinkingConfig;
    }

    @Getter
    @Builder
    public static class ThinkingConfig {
        @JsonProperty("thinkingBudget")
        private int thinkingBudget;  // 0 = thinking 비활성화
    }
}
