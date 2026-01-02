package com.educoon.domain.quiz.entity;

import com.educoon.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ai_quizzes")
public class AiQuiz{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "aiQuiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AiQuizQuestion> aiQuizQuestions = new ArrayList<>();

    public void addQuestion(AiQuizQuestion aiQuizQuestion){
        aiQuizQuestions.add(aiQuizQuestion);
        aiQuizQuestion.setAiQuiz(this);
    }


}
