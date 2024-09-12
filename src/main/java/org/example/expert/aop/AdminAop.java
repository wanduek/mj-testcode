package org.example.expert.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.example.expert.domain.user.entity.ApiUseTime;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.repository.ApiUseTimeRepository;
import org.example.expert.domain.user.repository.UserRepository;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j(topic = "AdminAop")
@Aspect
@Component
@RequiredArgsConstructor
public class AdminAop {

    private final ApiUseTimeRepository apiUseTimeRepository;
    private final UserRepository userRepository;


    @Pointcut("execution(* org.example.expert.domain.comment.commentcontroller.CommentAdminController.deleteComment(..))")
    private void commentDelete() {
    }

    @Pointcut("execution(* org.example.expert.domain.user.service.UserAdminService.changeUserRole(..))")
    private void userRoleChange() {
    }

    @Around("commentDelete() || userRoleChange()")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        //측정 시간
        long startTime = System.currentTimeMillis();

        // 요청 사항
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        String requestUrl = request.getRequestURI().toString();
        LocalDateTime requestTime = LocalDateTime.now(); // API 요청 시각
        Long userId = (Long) request.getAttribute("userId"); // Attribute에서 사용자 ID 가져오기

        // userId로 user 객체를 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없다."));

        try {
            // 핵심기능 수행
            Object output = joinPoint.proceed();
            return output;
        } finally {
            // 측정 종료 시간
            long endTime = System.currentTimeMillis();
            // 수행시간 = 종료 시간 - 시작 시간
            long runTime = endTime - startTime;


            log.info("[API 사용 시간] 사용자 ID: " + userId +
                    ", 총 소요 시간: " + runTime + " ms" +
                    ", 요청 URL: " + requestUrl +
                    ", 요청 Time: " + requestTime);

            // API 사용시간 및 DB에 기록
            ApiUseTime apiUseTime = apiUseTimeRepository.findByUser(user)
                    .orElse(new ApiUseTime(user, runTime));

            apiUseTime.addUseTime(runTime);
            apiUseTimeRepository.save(apiUseTime);
        }
    }
}


