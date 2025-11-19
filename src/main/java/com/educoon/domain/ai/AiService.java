package com.educoon.domain.ai;

import com.educoon.domain.gemini.GeminiApiService;
import com.educoon.domain.gemini.PdfParsingService;
import com.educoon.domain.studyRoom.StudyRoom;
import com.educoon.domain.studyRoom.StudyRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final GeminiApiService geminiApiService;
    private final PdfParsingService pdfParsingService;
    private final StudyRoomRepository studyRoomRepository;

    public Mono<String> chat(String message){
        if(message.contains("스터디") || message.contains("추천")){

            return recommendStudyRoom(message);
        }else{

            return geminiApiService.generateContent(message);
        }
    }

    public Mono<String> summarizeText(String text){
        String prompt = "다음 텍스트를 한국어로 요약해줘:\n\n" + text;
        return geminiApiService.generateContent(prompt);
    }

    public Mono<String> quizText(String text, QuestionType quizType) {

        // 1. 퀴즈 타입에 맞는 설명 생성
        String typeDescription = switch (quizType) {
            case MULTIPLE_CHOICE -> "객관식(MULTIPLE_CHOICE)";
            case TRUE_FALSE      -> "OX(TRUE_FALSE)";
            case SHORT_ANSWER    -> "단답형(SHORT_ANSWER)";
        };

        // 2. 동적 프롬프트 생성
        String prompt = String.format(
                "다음 텍스트를 기반으로 \"%s\" 유형의 퀴즈 5개를 생성해줘. " +
                        "반드시 다음 JSON 형식 배열로만 응답해줘. " +
                        "[{\"questionType\": \"%s\", \"questionText\": \"...\", \"options\": [...], \"answer\": \"...\"}]. " +
                        "다른 설명은 절대 포함하지 마. \n\n%s",
                typeDescription, quizType.name(), text // (예: "객관식", "MULTIPLE_CHOICE", "텍스트 원본...")
        );

        return geminiApiService.generateContent(prompt);
    }

    public Mono<String> processPdf(InputStream inputStream, String action, QuestionType quizType) {
        String text = pdfParsingService.extractText(inputStream);

        if ("summary".equals(action)) {
            return summarizeText(text);
        } else if ("quiz".equals(action)) {
            if (quizType == null) { // 퀴즈 액션인데 타입이 없으면 에러
                return Mono.error(new IllegalArgumentException("Quiz type is required for quiz action"));
            }
            // 3. 퀴즈 타입 전달
            return quizText(text, quizType);
        } else {
            return Mono.error(new IllegalArgumentException("Invalid action type"));
        }
    }

    private Mono<String> recommendStudyRoom(String message){
        String keywordPrompt = "다음 문장에서 스터디 주제(키워드) 1개만 추출해줘. (예: JPA, 토익, 정보처리기사): " + message;

        return geminiApiService.generateContent(keywordPrompt)
                .flatMap(keyword -> {
                    String trimmedKeyword = keyword.trim();
                    log.info("AI 추출 키워드: {}", trimmedKeyword);

                    List<StudyRoom> foundRooms = studyRoomRepository.findByTitleContaining(
                            trimmedKeyword, PageRequest.of(0, 3) // (최대 3개만)
                    ).getContent();

                    String finalPrompt;
                    if (foundRooms.isEmpty()) {
                        finalPrompt = message + "\n\n(참고: 위 질문에 대해 답변해줘. 아쉽게도 관련 스터디룸은 찾지 못했어.)";
                    } else {
                        String roomList = foundRooms.stream()
                                .map(room -> String.format("- %s (현재 %d명)", room.getTitle(), room.getParticipants().size())) // (참고: N+1 문제 발생 가능성 있음)
                                .collect(Collectors.joining("\n"));
                        finalPrompt = message + "\n\n(참고: 위 질문에 대해 답변해주고, 문장 마지막에 아래 스터디룸 목록을 추천해줘:\n" + roomList + ")";
                    }

                    return geminiApiService.generateContent(finalPrompt);
                });
    }
}
