package org.example.expert.domain.manager.managecontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.ManagerService;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;


import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ManageControllerTest {

    @MockBean
    private ManagerService managementService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ManagerService managerService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 매니저_등록_성공() throws Exception {

        // given
        long todoId = 1L;
        UserResponse userResponse = new UserResponse(1L, "user1@example.com");
        ManagerSaveRequest request = new ManagerSaveRequest(1L);
        ManagerSaveResponse response = new ManagerSaveResponse(1L, userResponse);

        AuthUser authUser = new AuthUser(1L, "test@example.com");
        String token = jwtUtil.createToken(authUser.getId(), authUser.getEmail(), UserRole.USER);

        when(managementService.saveManager(any(AuthUser.class), any(Long.class), any(ManagerSaveRequest.class)))
                .thenReturn(response);

        // When & then
        mockMvc.perform(MockMvcRequestBuilders.post("/todos/{todoId}/managers", todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.user.id").value(response.getUser().getId()))
                .andExpect(jsonPath("$.user.email").value(response.getUser().getEmail()));
    }

    @Test
    void 매니저_목록_조회_성공() throws Exception {

        // Given
        long todoId = 1L;
        UserResponse userResponse1 = new UserResponse(1L, "user1@example.com");
        UserResponse userResponse2 = new UserResponse(2L, "user1@example.com");
        ManagerResponse manager1 = new ManagerResponse(1L, userResponse1);
        ManagerResponse manager2 = new ManagerResponse(2L, userResponse2);
        List<ManagerResponse> managers = List.of(manager1, manager2);

        AuthUser authUser = new AuthUser(1L, "test@example.com");
        String token = jwtUtil.createToken(authUser.getId(), authUser.getEmail(), UserRole.USER);

        given(managerService.getManagers(todoId)).willReturn(managers);

        // When & Then
        mockMvc.perform(get("/todos/{todoId}/managers", todoId)
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void 매니저_삭제_성공() throws Exception {
        // Given
        long todoId = 1L;
        long managerId = 1L;

        AuthUser authUser = new AuthUser(1L, "test@example.com");
        String token = jwtUtil.createToken(authUser.getId(), authUser.getEmail(), UserRole.USER);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.delete("/todos/{todoId}/managers/{managerId}", todoId, managerId)
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

}