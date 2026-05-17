package com.testcasegenerator.controller;

import com.testcasegenerator.common.response.ApiResponse;
import com.testcasegenerator.dto.request.UserSettingsRequest;
import com.testcasegenerator.dto.response.UserDto;
import com.testcasegenerator.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDto>> getMe(@AuthenticationPrincipal String username) {
        return ResponseEntity.ok(ApiResponse.ok(userService.getUser(username)));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<UserDto>> updateSettings(
            @AuthenticationPrincipal String username,
            @RequestBody UserSettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateSettings(username, request)));
    }
}
