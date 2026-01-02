package com.educoon.domain.quiz.repository;

import com.educoon.domain.quiz.entity.AiQuiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiQuizRepository extends JpaRepository<AiQuiz, Long> {
    List<AiQuiz> findAllByUserUserId(Long userId);
}
