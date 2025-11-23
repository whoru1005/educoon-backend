package com.educoon.domain.ai;

import java.util.List;

public record QuizSaveRequest (
        String title,
        List<QuizQuestionDto> questions
){}
