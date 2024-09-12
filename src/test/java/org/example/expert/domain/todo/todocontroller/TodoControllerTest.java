package org.example.expert.domain.todo.todocontroller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TodoControllerTest {

    @MockBean
    private TodoService todoService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 게시물_저장_성공() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(1L, "user1@example.com");
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("Test Todo", "Test Contents");
        TodoSaveResponse todoSaveResponse = new TodoSaveResponse(1L, "Test Todo", "Test Contents", "Sunny", userResponse);

        AuthUser authUser = new AuthUser(1L, "test@example.com");
        String token = jwtUtil.createToken(authUser.getId(), authUser.getEmail(), UserRole.USER);

        given(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class)))
                .willReturn(todoSaveResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveRequest))
                        .header("AUTHORIZATION", token))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(todoSaveResponse.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value(todoSaveResponse.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.contents").value(todoSaveResponse.getContents()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.weather").value(todoSaveResponse.getWeather()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.id").value(todoSaveResponse.getUser().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email").value(todoSaveResponse.getUser().getEmail()));
    }


    @Test
    void 게시물_목록_조회_성공() throws Exception {
        // Given
        int page = 1;
        int size = 10;
        UserResponse userResponse1 = new UserResponse(1L, "user1@example.com");
        UserResponse userResponse2 = new UserResponse(2L, "user2@example.com");

        LocalDateTime createdAt1 = LocalDateTime.now();
        LocalDateTime modifiedAt1 = LocalDateTime.now();
        LocalDateTime createdAt2 = LocalDateTime.now();
        LocalDateTime modifiedAt2 = LocalDateTime.now();

        TodoResponse todoResponse1 = new TodoResponse(1L, "Test Todo1", "Test Contents1", "Sunny", userResponse1, createdAt1, modifiedAt1);
        TodoResponse todoResponse2 = new TodoResponse(2L, "Test Todo2", "Test Contents2", "Rainy", userResponse2, createdAt2, modifiedAt2);

        List<TodoResponse> todoList = List.of(todoResponse1, todoResponse2);
        Page<TodoResponse> todoPage = new PageImpl<>(todoList, PageRequest.of(page - 1, size), todoList.size());

        String token = jwtUtil.createToken(1L, "user1@example.com", UserRole.USER);

        given(todoService.getTodos(page, size)).willReturn(todoPage);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/todos")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .header("AUTHORIZATION", token))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(todoResponse1.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].title").value(todoResponse1.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].contents").value(todoResponse1.getContents()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].weather").value(todoResponse1.getWeather()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].user.id").value(todoResponse1.getUser().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].user.email").value(todoResponse1.getUser().getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].createdAt").value(createdAt1.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].modifiedAt").value(modifiedAt1.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].id").value(todoResponse2.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].title").value(todoResponse2.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].contents").value(todoResponse2.getContents()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].weather").value(todoResponse2.getWeather()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].user.id").value(todoResponse2.getUser().getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].user.email").value(todoResponse2.getUser().getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].createdAt").value(createdAt2.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[1].modifiedAt").value(modifiedAt2.toString()));


    }
}