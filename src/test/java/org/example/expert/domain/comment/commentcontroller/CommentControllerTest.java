package org.example.expert.domain.comment.commentcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private AuthUser authUser;

    @MockBean
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void comment_등록_성공() throws Exception {
        // Given
        long todoId = 1L;
        CommentSaveRequest commentSaveRequest = new CommentSaveRequest("아쎼이 기합!");
        UserResponse userResponse = new UserResponse(1L, "user1");
        CommentSaveResponse commentSaveResponse = new CommentSaveResponse(1L, "아쎼이 기합!", userResponse);

        AuthUser authUser = new AuthUser(1L, "user1");
        String token = jwtUtil.createToken(authUser.getId(), authUser.getEmail(), UserRole.USER);

        when(commentService.saveComment(any(AuthUser.class), anyLong(), any(CommentSaveRequest.class)))
                .thenReturn(commentSaveResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/todos/{todoId}/comments", todoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentSaveRequest))
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.contents").value("아쎼이 기합!"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email").value("user1"));
    }



    @Test
    void comment_목록_조회_성공() throws Exception {
        // Given
        long todoId = 1L;
        UserResponse userResponse1 = new UserResponse(1L, "user1");
        UserResponse userResponse2 = new UserResponse(2L, "user2");

        List<CommentResponse> commentResponses = Arrays.asList(
                new CommentResponse(1L, "기합!", userResponse1),
                new CommentResponse(2L, "기열!", userResponse2)
        );

        given(commentService.getComments(todoId)).willReturn(commentResponses);

        // When & Then
        mockMvc.perform(get("/todos/{todoId}/comments", todoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].contents").value("기합!"))
                .andExpect(jsonPath("$[0].user.id").value(1L))
                .andExpect(jsonPath("$[0].user.email").value("user1"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].contents").value("기열!"))
                .andExpect(jsonPath("$[1].user.id").value(2L))
                .andExpect(jsonPath("$[1].user.email").value("user2"));
    }
}
