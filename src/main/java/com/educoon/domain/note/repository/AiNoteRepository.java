package com.educoon.domain.note.repository;

import com.educoon.domain.note.entity.AiNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiNoteRepository extends JpaRepository<AiNote, Long> {
    List<AiNote> findAllByUserUserId(Long userId);
}
