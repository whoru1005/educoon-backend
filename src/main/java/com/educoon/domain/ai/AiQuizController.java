package com.educoon.domain.ai;

import com.educoon.domain.user.User;
import com.educoon.domain.user.UserRepository;
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
    private final UserRepository userRepository;

    @PostMapping("/save")
    public ResponseEntity<String> saveQuiz(@RequestBody QuizSaveRequest request) {

        // [디버깅 로그] 요청 데이터가 제대로 들어왔는지 서버 콘솔에 출력
        log.info("===== 퀴즈 저장 요청 데이터 확인 =====");
        log.info("Title: {}", request.title());
        if (request.questions() != null && !request.questions().isEmpty()) {
            request.questions().forEach(q ->
                    log.info("Question: Type={}, Text={}, Answer={}", q.questionType(), q.questionText(), q.answer())
            );
        } else {
            log.warn("Questions List is Empty or Null!");
        }
        log.info("==================================");


        String kakaoId = SecurityUtils.getCurrentUserKakaoId();

        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


        Long savedQuizId = aiQuizService.saveQuiz(user, request);

        return ResponseEntity.ok()
                .body("{\"message\": \"퀴즈 저장 성공\", \"quizId\": " + savedQuizId + "}");
    }
}