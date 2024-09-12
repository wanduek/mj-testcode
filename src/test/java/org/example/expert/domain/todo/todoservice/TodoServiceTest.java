package org.example.expert.domain.todo.todoservice;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class TodoServiceTest {

    @InjectMocks
    private TodoService todoService;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveTodo_정상적인_일정저장_테스트() {

        // given: 테스트에 필요한 데이터 설정
        AuthUser authUser = new AuthUser(1L, "odomarine@rokmc.com");
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("Test Title", "Test contents");
        User user = new User("odomarine@rokmc.com", "password", null);

        // 외부 서비스(WeatherClient)와 레포지토리(TodoRepository) 동작 설정
        when(weatherClient.getTodayWeather()).thenReturn("Sunny");
        when(todoRepository.save(any(Todo.class))).thenAnswer(invocation -> {
            Todo todo = invocation.getArgument(0);
            long todoId = 1L;
            return todo;
        });

        // when: 실제 서비스 메서드 호출
        TodoSaveResponse response = todoService.saveTodo(authUser, todoSaveRequest);

        // then: 결과 검증
        assertNotNull(response);  // 응답이 null이 아닌지 확인
        assertEquals("Test Title", response.getTitle());  // 제목이 예상한 값인지 확인
        assertEquals("Test contents", response.getContents());  // 내용이 예상한 값인지 확인
        assertEquals("Sunny", response.getWeather());  // 날씨가 예상한 값인지 확인
    }

    @Test
    void getTodos_정상적인_일정목록_가져오기_테스트() {

        // given: 페이지 정보와 Mock 데이터 설정
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User("odomarine@rokmc.com", "password", null);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        Page<Todo> todoPage = new PageImpl<>(Arrays.asList(todo));

        when(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).thenReturn(todoPage);

        // when: 일정 목록 조회 메서드 호출
        Page<TodoResponse> response = todoService.getTodos(1, 10);

        // then: 결과 검증
        assertNotNull(response);  // 응답이 null이 아닌지 확인
        assertEquals(1, response.getTotalElements());  // 조회된 일정 수 확인
        assertEquals("Title", response.getContent().get(0).getTitle());  // 첫 번째 일정의 제목 확인
    }

    @Test
    void getTodo_정상적인_일정_단건_가져오기_테스트() {

        // given: 특정 일정 데이터 설정
        User user = new User("odomarine@rokmc.com", "password", null);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", 1L);
        // findByIdWithUser()가 특정 ID로 Todo를 반환하도록 설정
        when(todoRepository.findByIdWithUser(anyLong())).thenReturn(Optional.of(todo));


        // when: 단일 일정 조회 메서드 호출
        TodoResponse response = todoService.getTodo(1L);

        // then: 결과 검증
        assertNotNull(response);  // 응답이 null이 아닌지 확인
        assertEquals(1L, response.getId());  // 조회된 일정의 ID 확인
        assertEquals("Title", response.getTitle());  // 일정 제목 확인
        assertEquals("Sunny", response.getWeather());  // 일정 날씨 확인
    }

    @Test
    void getTodo_존재하지_않는_일정_테스트() {
        // given: 일정이 존재하지 않는 경우 설정
        when(todoRepository.findByIdWithUser(1L)).thenReturn(Optional.empty());

        // when & then: 예외 발생 검증
        assertThrows(InvalidRequestException.class, () -> todoService.getTodo(1L));
    }
}
