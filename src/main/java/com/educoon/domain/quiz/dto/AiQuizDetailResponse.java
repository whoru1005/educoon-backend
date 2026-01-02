package com.educoon.domain.quiz.dto;

import com.educoon.domain.quiz.entity.AiQuizQuestion;
import com.educoon.domain.quiz.entity.AiQuiz;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class AiQuizDetailResponse {

    private Long quizId;
    private String title;
    private LocalDateTime createdAt;
    private List<QuestionDetail> questions;

    public AiQuizDetailResponse(AiQuiz quiz) {
        this.quizId = quiz.getQuizId();
        this.title = quiz.getTitle();
        this.createdAt = quiz.getCreatedAt();
        this.questions = quiz.getAiQuizQuestions().stream()
                .map(QuestionDetail::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class QuestionDetail {
        private String questionType;
        private String questionText;
        private String options;
        private String answer;

        public QuestionDetail(AiQuizQuestion q) {
            this.questionType = q.getQuestionType().name();
            this.questionText = q.getQuestionText();
            this.options = q.getOptions();
            this.answer = q.getAnswer();
        }
    }
}
