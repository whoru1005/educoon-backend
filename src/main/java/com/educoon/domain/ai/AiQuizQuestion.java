package com.educoon.domain.ai;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ai_quiz_questions")
public class AiQuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private AiQuiz aiQuiz;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestionType questionType;

    @Lob
    @Column(nullable = false)
    private String questionText;

    @Lob
    private String options;

    @Column(nullable = false)
    private String answer;
}
