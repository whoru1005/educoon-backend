package com.educoon.domain.ai.service;

import com.educoon.domain.quiz.entity.QuestionType;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.studyRoom.repository.StudyRoomRepository;
import com.educoon.infra.ai.GeminiApiService;
import com.educoon.infra.ai.PdfParsingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private GeminiApiService geminiApiService;
    @Mock
    private PdfParsingService pdfParsingService;
    @Mock
    private StudyRoomRepository studyRoomRepository;

    @InjectMocks
    private AiService aiService;

    @Test
    @DisplayName("일반 채팅 질문 처리")
    void chat_General_Success() {
        // given
        String message = "안녕";
        given(geminiApiService.generateContent(message)).willReturn(Mono.just("안녕하세요!"));

        // when
        Mono<String> result = aiService.chat(message);

        // then
        StepVerifier.create(result)
                .expectNext("안녕하세요!")
                .verifyComplete();
    }

    @Test
    @DisplayName("텍스트 요약 성공")
    void summarizeText_Success() {
        // given
        String text = "긴 텍스트...";
        given(geminiApiService.generateContent(anyString())).willReturn(Mono.just("요약본"));

        // when
        Mono<String> result = aiService.summarizeText(text);

        // then
        StepVerifier.create(result)
                .expectNext("요약본")
                .verifyComplete();
    }

    @Test
    @DisplayName("퀴즈 생성 성공")
    void quizText_Success() {
        // given
        String text = "학습 내용";
        QuestionType type = QuestionType.MULTIPLE_CHOICE;
        given(geminiApiService.generateJsonContent(anyString(), anyString())).willReturn(Mono.just("[{\"question\": \"...\"}]"));

        // when
        Mono<String> result = aiService.quizText(text, type);

        // then
        StepVerifier.create(result)
                .expectNext("[{\"question\": \"...\"}]")
                .verifyComplete();
    }

    @Test
    @DisplayName("스터디룸 추천 처리")
    void recommendStudyRoom_Success() {
        // given
        String message = "스프링 스터디 추천해줘";
        given(geminiApiService.generateContent(contains("핵심 검색 키워드"))).willReturn(Mono.just("스프링"));
        given(studyRoomRepository.findByKeywordWithTags(eq("스프링"), any())).willReturn(Collections.emptyList());
        given(geminiApiService.generateContent(contains("현재 개설된 방이 하나도 없어"))).willReturn(Mono.just("추천 결과 없음"));

        // when
        Mono<String> result = aiService.chat(message);

        // then
        StepVerifier.create(result)
                .expectNext("추천 결과 없음")
                .verifyComplete();
    }
}
