package com.testcasegenerator.dto.response;

import com.testcasegenerator.domain.User;
import lombok.Getter;

@Getter
public class UserDto {
    private final String username;
    private final String displayName;
    private final String preferredAiProvider;

    public UserDto(User user) {
        this.username = user.getUsername();
        this.displayName = user.getDisplayName();
        this.preferredAiProvider = user.getPreferredAiProvider();
    }
}
