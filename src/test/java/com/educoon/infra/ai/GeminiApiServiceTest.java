package com.educoon.infra.ai;

import com.educoon.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class GeminiApiServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    private GeminiApiService geminiApiService;

    @BeforeEach
    void setUp() {
        geminiApiService = new GeminiApiService(webClientBuilder);
        ReflectionTestUtils.setField(geminiApiService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(geminiApiService, "apiUrl", "http://test-api.com");
    }

    @Test
    @DisplayName("Gemini API 호출 성공")
    void generateContent_Success() {
        // given
        String prompt = "안녕";
        String expectedResponse = "안녕하세요!";
        
        WebClient webClient = mock(WebClient.class);
        given(webClientBuilder.baseUrl(anyString())).willReturn(webClientBuilder);
        given(webClientBuilder.defaultHeader(anyString(), any())).willReturn(webClientBuilder);
        given(webClientBuilder.build()).willReturn(webClient);
        
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.bodyValue(any())).willReturn(requestHeadersSpec);
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        
        GeminiResponse mockResponse = new GeminiResponse(new Candidate[]{
                new Candidate(new Content(new Part[]{new Part(expectedResponse)}, "model"))
        });
        given(responseSpec.bodyToMono(GeminiResponse.class)).willReturn(Mono.just(mockResponse));

        // when
        Mono<String> result = geminiApiService.generateContent(prompt);

        // then
        StepVerifier.create(result)
                .expectNext(expectedResponse)
                .verifyComplete();
    }

    @Test
    @DisplayName("JSON 응답 정제 테스트")
    void cleanJson_Test() {
        // given
        String jsonWithMarkdown = "```json\n[{\"key\": \"value\"}]\n```";
        
        // Reflection을 사용하여 private 메서드 테스트 (또는 public 메서드를 통해 테스트)
        String cleaned = ReflectionTestUtils.invokeMethod(geminiApiService, "cleanJson", jsonWithMarkdown);

        // then
        assertThat(cleaned).isEqualTo("[{\"key\": \"value\"}]");
    }
}
