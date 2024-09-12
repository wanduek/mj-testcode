package org.example.expert.domain.user.userservice;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class UseServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User("test@example.com", new PasswordEncoder().encode("q1w2e3r4"), null);
        ReflectionTestUtils.setField(user, "id", 1L);
    }

    @Test
    void getUser_정상적으로_사용지_조회() {

        // given
        when(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user));

        // when
        UserResponse response = userService.getUser(1L);

        // then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void changePassword_정상적인_비밀번호_변경() {

        // given
        String oldPassword = "OldPassword123";
        String newPassword = "NewPassword123";
        User user = new User();
        String encodedOldPassword = new PasswordEncoder().encode(oldPassword);

        ReflectionTestUtils.setField(user, "password", encodedOldPassword);

        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenAnswer(invocation -> {
            String providedPassword = invocation.getArgument(0);
            String storedPassword = invocation.getArgument(1);
            return new PasswordEncoder().matches(providedPassword, storedPassword);
        });
        when(passwordEncoder.encode(any(String.class))).thenAnswer(invocation -> {
            String password = invocation.getArgument(0);
            return new PasswordEncoder().encode(password);
        });
        // when
        userService.changePassword(1L, request);

        // then
        verify(userRepository).save(user);
        assertTrue(passwordEncoder.matches(newPassword, user.getPassword()));
    }

    @Test
    void changePassword_현재_비밀번호_불일치_경우_예외() {
        // given
        String oldPassword = "OldPassword123";
        String newPassword = "NewPassword123";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPassword, newPassword);

        User user = new User();
        String encodedOldPassword = new PasswordEncoder().encode("WrongOldPassword");

        ReflectionTestUtils.setField(user, "password", encodedOldPassword);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq(oldPassword),any(String.class))).thenReturn(false);

        // when & then
        InvalidRequestException thrown = assertThrows(InvalidRequestException.class, () -> {
            userService.changePassword(1L, request);
        });
        assertEquals("현재 비밀번호가 일치하지 않습니다.", thrown.getMessage());
    }
}
