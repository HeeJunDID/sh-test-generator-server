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
    }
}
