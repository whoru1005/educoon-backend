package com.educoon.domain.quiz.dto;

import java.util.List;

public record QuizQuestionDto(
        String questionType,
        String questionText,
        List<String> options,
        String answer
) {
}
