package com.educoon.domain.ai;

import com.educoon.domain.aiNote.AiNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiQuizRepository extends JpaRepository<AiQuiz, Long> {
    List<AiQuiz> findAllByUserUserId(Long userId);
}
