package com.educoon.domain.quiz.controller;

import com.educoon.domain.quiz.service.AiQuizService;
import com.educoon.domain.quiz.dto.QuizSaveRequest;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.repository.UserRepository;
import com.educoon.domain.user.service.UserService;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import com.educoon.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/ai/quizzes")
@RequiredArgsConstructor
public class AiQuizController {

    private final AiQuizService aiQuizService;
    private final UserService userService;

    @PostMapping("/save")
    public ResponseEntity<String> saveQuiz(@RequestBody QuizSaveRequest request) {

        log.debug("Title: {}", request.title());

        if (request.questions() != null && !request.questions().isEmpty()) {
            request.questions().forEach(q ->
                    log.debug("Question: Type={}, Text={}, Answer={}", q.questionType(), q.questionText(), q.answer())
            );
        } else {
            log.warn("Questions List is Empty or Null!");
        }
        log.info("==================================");


        String kakaoId = SecurityUtils.getCurrentUserKakaoId();

        User user = userService.findByKakaoId(kakaoId);


        Long savedQuizId = aiQuizService.saveQuiz(user, request);

        return ResponseEntity.ok()
                .body("{\"message\": \"퀴즈 저장 성공\", \"quizId\": " + savedQuizId + "}");
    }
}