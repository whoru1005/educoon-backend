package com.educoon.domain.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record QuizQuestionDto(
        String questionType,
        String questionText,
        List<String> options,
        String answer
) {
}
