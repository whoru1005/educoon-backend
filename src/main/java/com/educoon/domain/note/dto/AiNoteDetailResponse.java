package com.educoon.domain.note.dto;

import com.educoon.domain.note.entity.AiNote;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AiNoteDetailResponse {
    private Long noteId;
    private String title;
    private String content;        // 핵심: 요약된 본문 내용
    private String originalFileRef;
    private LocalDateTime createdAt;

    public AiNoteDetailResponse(AiNote note) {
        this.noteId = note.getNoteId();
        this.title = note.getTitle();
        this.content = note.getSummaryContent();
        this.originalFileRef = note.getOriginalFileRef();
        this.createdAt = note.getCreatedAt();
    }
}
