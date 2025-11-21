package com.educoon.domain.aiNote;

public record AiNoteSaveRequest(
    String title,
    String content,
    String originalFileRef
){}
