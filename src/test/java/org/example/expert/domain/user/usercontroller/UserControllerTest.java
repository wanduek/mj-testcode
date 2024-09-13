package org.example.expert.domain.user.usercontroller;

import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    @Test
    void 유저_정보_조회_성공() throws Exception {
        // Given
        long userId = 1L;
        UserResponse userResponse = new UserResponse(userId, "test@example.com");

        AuthUser authUser = new AuthUser(1L, "test@example.com");
        String token =jwtUtil.createToken(authUser.getId(), authUser.getEmail(), UserRole.USER);

        given(userService.getUser(userId)).willReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/users/{userId}", userId)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userResponse.getId()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()));
    }

}
