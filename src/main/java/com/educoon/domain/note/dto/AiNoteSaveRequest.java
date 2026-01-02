package com.educoon.domain.note.dto;

public record AiNoteSaveRequest(
    String title,
    String content,
    String originalFileRef
){}
