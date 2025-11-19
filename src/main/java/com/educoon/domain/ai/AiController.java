package com.educoon.domain.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

// 요청 DTO (Inner Record)
record ChatRequest(String message){}
record TextRequest(String text){}

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    // Gemini API 응답 대기 시간 설정 (30초)
    private static final Duration AI_TIMEOUT = Duration.ofSeconds(30);

    /**
     * 1:1 AI 채팅 및 스터디룸 추천
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request){

        // block()을 사용하여 결과가 나올 때까지 기다림 (동기 처리)
        String responseBody = aiService.chat(request.message())
                .block(AI_TIMEOUT);

        // JSON 형태로 응답
        return ResponseEntity.ok().body("{\"response\": \"" + responseBody + "\"}");
    }

    /**
     * 텍스트 요약
     */
    @PostMapping("/summarize-text")
    public ResponseEntity<String> summarizeText(@RequestBody TextRequest textRequest){

        String summary = aiService.summarizeText(textRequest.text())
                .block(AI_TIMEOUT);

        return ResponseEntity.ok().body("{\"summary\": \"" + summary + "\"}");
    }

    /**
     * 텍스트로 퀴즈 생성
     */
    @PostMapping("/quiz-text")
    public ResponseEntity<String> quizText(
            @RequestBody TextRequest request,
            @RequestParam("quizType") QuestionType quizType
    ) {
        // [수정] .map() 대신 .block() 사용 -> String 반환
        String quizJson = aiService.quizText(request.text(), quizType)
                .block(AI_TIMEOUT);

        return ResponseEntity.ok().body(quizJson);
    }

    /**
     * PDF 업로드 및 처리 (요약 or 퀴즈)
     */
    @PostMapping("/upload-pdf")
    public ResponseEntity<String> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("action") String action, // "summary" 또는 "quiz"
            @RequestParam(name = "quizType", required = false) QuestionType quizType
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"파일이 비어있습니다.\"}");
        }

        try {
            // [수정] .map() 대신 .block() 사용 -> String 반환
            String responseBody = aiService.processPdf(file.getInputStream(), action, quizType)
                    .block(AI_TIMEOUT);

            return ResponseEntity.ok().body(responseBody);

        } catch (IOException e) {
            throw new RuntimeException("PDF 파일 처리 중 오류 발생", e);
        }
    }
}