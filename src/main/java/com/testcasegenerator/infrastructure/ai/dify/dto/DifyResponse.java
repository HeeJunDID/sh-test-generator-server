package com.testcasegenerator.infrastructure.ai.dify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DifyResponse {

    @JsonProperty("workflow_run_id")
    private String workflowRunId;

    @JsonProperty("task_id")
    private String taskId;

    private Data data;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        private String id;
        private String status;
        private Outputs outputs;
        private String error;
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Outputs {
        private List<Map<String, Object>> text;
    }

    public List<Map<String, Object>> extractTestCases() {
        if (data == null || data.getOutputs() == null) {
            return List.of();
        }
        return data.getOutputs().getText() != null ? data.getOutputs().getText() : List.of();
    }

    public String getStatus() {
        return data != null ? data.getStatus() : null;
    }

    public String getError() {
        return data != null ? data.getError() : null;
    }
}
