package com.educoon.domain.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
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

    // Gemini API 응답 대기 시간 설정 (30초)
    private static final Duration AI_TIMEOUT = Duration.ofSeconds(30);


    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request){

        String responseBody = aiService.chat(request.message())
                .block(AI_TIMEOUT);

        // [수정] 수동 문자열 조립 -> Map 반환 (Spring이 알아서 JSON 변환 및 이스케이프 처리)
        return ResponseEntity.ok(Map.of("response", responseBody));
    }

    /**
     * 텍스트 요약
     * [반환] {"summary": "내용..."}
     */
    @PostMapping("/summarize-text")
    public ResponseEntity<Map<String, String>> summarizeText(@RequestBody TextRequest textRequest){

        String summary = aiService.summarizeText(textRequest.text())
                .block(AI_TIMEOUT);

        return ResponseEntity.ok(Map.of("summary", summary));
    }


    @PostMapping("/quiz-text")
    public ResponseEntity<Object> quizText(
            @RequestBody TextRequest request,
            @RequestParam("quizType") QuestionType quizType
    ) {
        // [수정] 서비스에서 이미 cleanJson 된 문자열이 넘어옴
        String quizJsonString = aiService.quizText(request.text(), quizType)
                .block(AI_TIMEOUT);

        try {
            // 바로 파싱하면 됨
            Object jsonObject = objectMapper.readValue(quizJsonString, Object.class);
            return ResponseEntity.ok(jsonObject);
        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 실패. 원본: {}", quizJsonString);
            return ResponseEntity.internalServerError().body(Map.of("error", "AI 응답 파싱 실패"));
        }
    }

    /**
     * PDF 업로드 및 처리 (요약 or 퀴즈)
     */

    @PostMapping("/upload-pdf")
    public ResponseEntity<Object> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("action") String action,
            @RequestParam(name = "quizType", required = false) QuestionType quizType
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "파일이 비어있습니다."));
        }

        try {
            String responseBody = aiService.processPdf(file.getInputStream(), action, quizType)
                    .block(AI_TIMEOUT);

            if ("quiz".equals(action)) {
                // [수정] 여기도 마찬가지로 바로 파싱
                Object jsonObject = objectMapper.readValue(responseBody, Object.class);
                return ResponseEntity.ok(jsonObject);
            } else {
                return ResponseEntity.ok(Map.of("summary", responseBody));
            }

        } catch (IOException e) {
            throw new RuntimeException("PDF 파일 처리 중 오류 발생", e);
        }
    }

}