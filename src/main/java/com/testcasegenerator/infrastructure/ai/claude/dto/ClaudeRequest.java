package com.testcasegenerator.infrastructure.ai.claude.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ClaudeRequest {

    private String model;

    @JsonProperty("max_tokens")
    private int maxTokens;

    private String system;

    private List<Message> messages;

    @Getter
    @Builder
    public static class Message {
        private String role;
        private String content;
    }
}
