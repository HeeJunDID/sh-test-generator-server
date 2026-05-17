package com.testcasegenerator.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserSettingsRequest {
    private String preferredAiProvider; // dify | gemini | claude
}
