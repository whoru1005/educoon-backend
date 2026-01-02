package com.educoon.domain.quiz.dto;

import java.util.List;

public record QuizSaveRequest (
        String title,
        List<QuizQuestionDto> questions
){}
