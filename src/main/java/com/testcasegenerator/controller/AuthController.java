package com.testcasegenerator.controller;

import com.testcasegenerator.common.exception.BusinessException;
import com.testcasegenerator.common.response.ApiResponse;
import com.testcasegenerator.domain.User;
import com.testcasegenerator.domain.UserRepository;
import com.testcasegenerator.dto.request.LoginRequest;
import com.testcasegenerator.dto.response.LoginResponse;
import com.testcasegenerator.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("아이디 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ResponseEntity.ok(ApiResponse.ok(
                new LoginResponse(token, user.getUsername(), user.getDisplayName(), user.getPreferredAiProvider(), user.getRole())
        ));
    }
}
