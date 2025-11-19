package com.educoon.domain.ai;

import java.util.List;

public record QuizSaveRequest (
        String title,
        String originalFileRef,
        List<QuizQuestionDto> questions
){}
