package com.educoon.domain.gemini;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

record SafetySetting(String category, String threshold) {}

record Part(String text) {}

// [!!] 요청(Request)용 Content 객체를 새로 정의합니다.
record ContentRequest(Part[] parts) {}

// [!!] GeminiRequest가 ContentRequest 배열을 갖도록 수정합니다.
record GeminiRequest(ContentRequest[] contents, SafetySetting[] safetySettings) {}

// (응답 DTO는 기존과 동일)
record GeminiResponse(Candidate[] candidates) {}
record Candidate(Content content) {}
record Content(Part[] parts, String role) {}

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiApiService {

    private final WebClient.Builder webClientBuilder;

    @Value("${google.gemini.api.key}")
    private String apiKey;

    @Value("${google.gemini.api.url}")
    private String apiUrl;


    private static final SafetySetting[] DEFAULT_SAFETY_SETTINGS = new SafetySetting[]{
            new SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_NONE"),
            new SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_NONE"),
            new SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_NONE"),
            new SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_NONE")
    };


    public Mono<String> generateContent(String prompt) {
        WebClient webClient = webClientBuilder.baseUrl(apiUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-goog-api-key", apiKey)
                .build();


        GeminiRequest requestBody = new GeminiRequest(
                new ContentRequest[]{
                        new ContentRequest(
                                new Part[]{new Part(prompt)}
                        )
                },
                DEFAULT_SAFETY_SETTINGS
        );


        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .map(geminiResponse -> {
                    try {
                        System.out.println(geminiResponse.candidates()[0].content().parts()[0].text());
                        return geminiResponse.candidates()[0].content().parts()[0].text();
                    } catch (Exception e) {
                        log.error("Gemini API 응답 파싱 실패", e);
                        throw new RuntimeException("AI 응답을 처리하는 중 오류가 발생");
                    }
                })
                .doOnError(error -> {
                    log.error("Gemini API 호출 실패: {}", error.getMessage());
                });
    }

    public Mono<String> generateJsonContent(String prompt, String instructions) {
        String jsonPrompt = String.format(
                "IMPORTANT: Respond ONLY with a valid raw JSON array. \n" +
                        "Format: [{\"questionType\": \"...\", \"questionText\": \"...\", \"options\": [...], \"answer\": \"...\"}]. \n" +
                        "Do NOT use markdown code blocks (```json). Just return the raw JSON string. \n\n" +
                        "%s \n\n" +
                        "[Text]: \n%s",
                instructions, prompt
        );

        // generateContent 호출 후 -> 결과값(text)이 오면 -> cleanJson으로 씻어서 반환
        return generateContent(jsonPrompt)
                .map(this::cleanJson);
    }

    // [유틸 메서드] 백틱 제거 로직 (이 클래스 안에 추가하세요)
    private String cleanJson(String text) {
        if (text == null) return "[]";

        String cleaned = text.trim();

        // 1. 시작 부분의 ```json 또는 ``` 제거
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```json", "").replaceAll("^```", "");
        }

        // 2. 끝 부분의 ``` 제거
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.replaceAll("```$", "");
        }

        return cleaned.trim();
    }
}