package com.testcasegenerator.infrastructure.ai.dify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class DifyRequest {

    private Map<String, Object> inputs;

    @JsonProperty("response_mode")
    private String responseMode;

    private String user;
}
