package org.example.expert.domain.comment.service;

import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ManagerService managerService;
    @Mock
    private Comment comment;
    @InjectMocks
    private CommentService commentService;


    public CommentServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void comment_등록_중_할일을_찾지_못해_에러가_발생한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email");

        given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            commentService.saveComment(authUser, todoId, request);
        });

        // then
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void comment를_정상적으로_등록한다() {
        // given
        long todoId = 1;
        CommentSaveRequest request = new CommentSaveRequest("contents");
        AuthUser authUser = new AuthUser(1L, "email");
        User user = User.fromAuthUser(authUser);
        Todo todo = new Todo("title", "title", "contents", user);
        Comment comment = new Comment(request.getContents(), user, todo);

        given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
        given(commentRepository.save(any())).willReturn(comment);

        // when
        CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

        // then
        assertNotNull(result);
    }

//    @Test
//    void 유저가_담당자가_아닐_경우_댓글_저장_실패() {
//
//        // Given
//        long todoId = 1L;
//        long userId = 1L;
//        AuthUser authUser = new AuthUser(userId, "test@example.com");
//
//        CommentSaveRequest commentSaveRequest = new CommentSaveRequest();
//        ReflectionTestUtils.setField(commentSaveRequest, "contents","test content");
//
//        given(todoRepository.findById(todoId)).willReturn(Optional.of(new Todo()));
//        given(managerService.isManagerForTodo(userId, todoId)).willReturn(false);
//
//        InvalidRequestException thrown = assertThrows(InvalidRequestException.class, () -> {
//            commentService.saveComment(authUser, todoId, commentSaveRequest);
//        });
//
//        // When & Then
//        assertEquals("게시물에 허락받은 유저가 아니면 댓글달기 불가.", thrown.getMessage());
//
//        verify(commentRepository, never()).save(any(Comment.class)); // 댓글 저장되지 않음
//    }

//    @Test
//    void 유저가_담당자인_경우_댓글_저장_성공() {
//        // Given
//        long todoId = 1L;
//        long userId = 1L;
//
//        CommentSaveRequest commentSaveRequest = new CommentSaveRequest("test content");
//        AuthUser authUser = new AuthUser(userId, "test@example.com");
//        User user = User.fromAuthUser(authUser);
//        Todo todo = new Todo();
//        ReflectionTestUtils.setField(todo, "id", todoId);
//        ReflectionTestUtils.setField(todo, "user", user);
//
//        Comment comment = new Comment();
//        ReflectionTestUtils.setField(comment, "contents", commentSaveRequest.getContents());
//        ReflectionTestUtils.setField(comment, "user", user);
//        ReflectionTestUtils.setField(comment, "todo", todo);
//
//        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
//        given(managerService.isManagerForTodo(userId, todoId)).willReturn(true);
//        given(commentRepository.save(any(Comment.class))).willReturn(comment);
//
//        // When
//        CommentResponse response = commentService.saveComment(authUser, todoId, commentSaveRequest);
//
//        // Then
//        assertEquals("test content", response.getContents());
//        assertEquals(userId, response.getUser().getId());
//        assertEquals("test@example.com", response.getUser().getEmail());
//        verify(commentRepository).save(any(Comment.class));
//    }

}
