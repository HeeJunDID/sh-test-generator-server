package com.testcasegenerator.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String username;
    private String displayName;
    private String preferredAiProvider;
}
