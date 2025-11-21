package com.educoon.domain.aiNote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiNoteRepository extends JpaRepository<AiNote, Long> {
    List<AiNote> findAllByUserUserId(Long userId);
}
