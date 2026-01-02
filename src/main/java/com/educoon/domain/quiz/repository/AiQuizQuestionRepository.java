package com.educoon.domain.quiz.repository;

import com.educoon.domain.quiz.entity.AiQuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiQuizQuestionRepository extends JpaRepository<AiQuizQuestion, Long> {
}
