package com.educoon.domain.ai;

import com.educoon.domain.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AiQuizService {

    private final AiQuizRepository aiQuizRepository;
    private final ObjectMapper objectMapper;

    public Long saveQuiz(User user, QuizSaveRequest request) {
        // 1. 퀴즈 묶음(부모) 엔티티 생성
        AiQuiz quiz = AiQuiz.builder()
                .user(user)
                .title(request.title())
                .build();

        // 2. 문항(자식) 엔티티 변환 및 추가
        if (request.questions() != null) {
            for (QuizQuestionDto qDto : request.questions()) {
                try {
                    // List<String> -> JSON String 변환
                    String optionsJson = objectMapper.writeValueAsString(qDto.options());

                    AiQuizQuestion question = AiQuizQuestion.builder()
                            .questionType(QuestionType.valueOf(qDto.questionType()))

                            // [!!!!] 여기가 문제였습니다. 이 줄이 없으면 DB 에러가 납니다.
                            .questionText(qDto.questionText())

                            .options(optionsJson)
                            .answer(qDto.answer())
                            .build();

                    // 연관관계 편의 메서드 (AiQuiz에 추가)
                    quiz.addQuestion(question);

                } catch (JsonProcessingException e) {
                    log.error("퀴즈 보기 JSON 변환 실패", e);
                    throw new RuntimeException("퀴즈 데이터 처리 중 오류 발생");
                }
            }
        }

        // 3. 저장 (Cascade 설정으로 인해 문항들도 함께 저장됨)
        AiQuiz savedQuiz = aiQuizRepository.save(quiz);
        return savedQuiz.getQuizId();
    }
}