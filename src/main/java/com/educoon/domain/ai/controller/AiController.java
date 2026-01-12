package com.educoon.domain.ai.controller;

import com.educoon.domain.ai.service.AiService;
import com.educoon.domain.quiz.dto.QuizQuestionDto;
import com.educoon.domain.quiz.entity.QuestionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

// 요청 DTO (Inner Record)
record ChatRequest(String message){}
record TextRequest(String text){}

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
@Slf4j
public class AiController {

    private final AiService aiService;
    private final ObjectMapper objectMapper; // JSON 변환기 (Spring이 자동 주입)

    @Value("${ai.timeout.seconds:30}")
    private int aiTimeoutSeconds;

    private Duration getAiTimeout() {
        return Duration.ofSeconds(aiTimeoutSeconds);
    }


    @PostMapping("/chat")
    public Mono<ResponseEntity<Map<String, String>>> chat(@RequestBody ChatRequest request){
        return aiService.chat(request.message())
                .map(response -> ResponseEntity.ok(Map.of("response", response)))
                .timeout(getAiTimeout())
                .onErrorResume(e ->{
                    log.error("AI 응답 시간 초과 또는 오류 발생", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "AI 응답 시간 초과 또는 오류 발생")));
                });
    }

    /**
     * 텍스트 요약
     * [반환] {"summary": "내용..."}
     */
    @PostMapping("/summarize-text")
    public Mono<ResponseEntity<Map<String, String>>> summarizeText(@RequestBody TextRequest textRequest){
        return aiService.summarizeText(textRequest.text())
                .map(summary ->ResponseEntity.ok(Map.of("summary", summary)))
                .timeout(getAiTimeout())
                .onErrorResume(e ->{
                    log.error("AI 요약 시간 초과 또는 오류 발생", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", "AI 요약 시간 초과 또는 오류 발생")));
                });
    }


    @PostMapping("/quiz-text")
    public Mono<ResponseEntity<List<QuizQuestionDto>>> quizText(
            @RequestBody TextRequest request,
            @RequestParam("quizType") QuestionType quizType
    ) {
        return aiService.quizText(request.text(), quizType)
                .timeout(getAiTimeout())
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("AI quiz error", e);
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .build());
                });
    }

    /**
     * PDF 업로드 및 처리 (요약 or 퀴즈)
     */

    @PostMapping("/upload-pdf")
    public Mono<ResponseEntity<?>> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("action") String action,
            @RequestParam(name = "quizType", required = false) QuestionType quizType
    ) {
        if (file.isEmpty()) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(Map.of("error", "파일이 비어있습니다.")));
        }

        try {
            return aiService.processPdf(file.getInputStream(), action, quizType)
                    .timeout(getAiTimeout())
                    .map(responseBody -> {
                        if ("quiz".equals(action)) {
                            return ResponseEntity.ok(responseBody);
                        } else {
                            return ResponseEntity.ok(Map.of("summary", responseBody));
                        }
                    })
                    .onErrorResume(e -> {
                        log.error("PDF 처리 오류", e);
                        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .build());
                    });
        } catch (IOException e) {
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 읽기 오류")));
        }
    }
}