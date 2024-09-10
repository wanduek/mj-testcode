package org.example.expert.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUser(long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new InvalidRequestException("User not found"));
        return new UserResponse(user.getId(), user.getEmail());
    }

    @Transactional
    public void changePassword(long userId, UserChangePasswordRequest userChangePasswordRequest) {

        // 새 비밀번호 형식 유효성 검사
        validateUserChangePasswordRequest(userChangePasswordRequest);

        // 데이터베이스에서 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRequestException("사용자를 찾을 수 없습니다."));

        // 새 비밀번호가 현재 비밀번호와 동일한지 확인
        if (passwordEncoder.matches(userChangePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new InvalidRequestException("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.");
        }

        // 제공된 현재 비밀번호가 맞는지 확인
        if (!passwordEncoder.matches(userChangePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new InvalidRequestException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 변경 및 저장
        user.changePassword(passwordEncoder.encode(userChangePasswordRequest.getNewPassword()));
        userRepository.save(user); // 변경 사항 저장
    }

    private void validateUserChangePasswordRequest(UserChangePasswordRequest userChangePasswordRequest) {
        String newPassword = userChangePasswordRequest.getNewPassword();
        if (newPassword.length() < 8 ||
                !newPassword.matches(".*\\d.*") || // 숫자 포함 여부 확인
                !newPassword.matches(".*[A-Z].*")) { // 대문자 포함 여부 확인
            throw new InvalidRequestException("새 비밀번호는 8자 이상이어야 하며, 숫자와 대문자를 포함해야 합니다.");
        }
    }


}

