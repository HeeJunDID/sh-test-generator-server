package com.testcasegenerator.service;

import com.testcasegenerator.dto.request.UserSettingsRequest;
import com.testcasegenerator.dto.response.UserDto;

public interface UserService {
    UserDto getUser(String username);
    UserDto updateSettings(String username, UserSettingsRequest request);
}
