package org.example.expert.domain.auth.authservice;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    void signup_정상적인_회원가입_테스트() {
        // given
        SignupRequest signupRequest = new SignupRequest("test@example.com", "password123", "USER");

        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "email", "test@example.com");
        ReflectionTestUtils.setField(user, "password", "encodedPassword");
        ReflectionTestUtils.setField(user, "userRole", UserRole.USER);

        String bearerToken = "someBearerToken";

        // when
        // Mock user repository behavior
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false); // 수정: 새로운 사용자 등록 가능
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword"); // 수정: 실제 인코딩된 패스워드 반환
        when(userRepository.save(any(User.class))).thenReturn(user); // Mock으로 저장된 사용자 반환
        when(jwtUtil.createToken(1L, "test@example.com", UserRole.USER)).thenReturn(bearerToken); // Mock으로 JWT 반환

        SignupResponse response = authService.signup(signupRequest);

        // then
        assertNotNull(response.getBearerToken());
        assertEquals(bearerToken, response.getBearerToken());
        verify(userRepository).save(any(User.class));
    }



    @Test
    void signin_정상적인_로그인_테스트() {
        // given
        String email = "test@example.com";
        String password = "1q2w3e4r";
        UserRole userRole = UserRole.USER;
        String encodedPassword = passwordEncoder.encode(password);
        String bearerToken = "someBearerToken";

        // Create user and mock repository behavior
        User user = new User(email, encodedPassword, userRole);

        // Mocking UserRepository
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.createToken(user.getId(), email, userRole)).thenReturn(bearerToken);

        // Set up SigninRequest
        SigninRequest signinRequest = new SigninRequest(email, password);

        // when
        SigninResponse response = authService.signin(signinRequest);

        // then
        assertNotNull(response.getBearerToken());
        assertEquals(bearerToken, response.getBearerToken());
    }


    @Test
    void signin_존재하지_않는_유저_로그인_테스트() {
        // given
        SigninRequest signinRequest = new SigninRequest();

        User user = new User();
        ReflectionTestUtils.setField(user, "email", "odomarine@gmail.com");
        ReflectionTestUtils.setField(user, "password" , "abc1234@");


        // when & then
        assertThrows(InvalidRequestException.class, () -> authService.signin(signinRequest));
    }

    @Test
    void signin_잘못된_비밀번호_테스트() {
        // given
        String email = "odomarine@gmail.com";
        String password = "1q2w3e4r";
        UserRole userRole = UserRole.USER;

        // 유저 생성
        User user = new User(email, passwordEncoder.encode(password), userRole);
        userRepository.save(user);

        SigninRequest signinRequest = new SigninRequest();
        ReflectionTestUtils.setField(user, "email", "odomarine@gmail.com");
        ReflectionTestUtils.setField(user, "password" , "abc1234@");

        // when & then
        assertThrows(InvalidRequestException.class, () -> authService.signin(signinRequest));
    }
}
