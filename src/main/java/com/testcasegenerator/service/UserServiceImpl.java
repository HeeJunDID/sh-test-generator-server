package com.testcasegenerator.service;

import com.testcasegenerator.common.exception.BusinessException;
import com.testcasegenerator.domain.User;
import com.testcasegenerator.domain.UserRepository;
import com.testcasegenerator.dto.request.UserSettingsRequest;
import com.testcasegenerator.dto.response.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto getUser(String username) {
        User user = findUser(username);
        return new UserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateSettings(String username, UserSettingsRequest request) {
        User user = findUser(username);
        user.updateSettings(request.getPreferredAiProvider());
        return new UserDto(user);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다."));
    }
}
