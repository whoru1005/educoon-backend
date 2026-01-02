package com.educoon.domain.ai.service;

import com.educoon.domain.quiz.entity.QuestionType;
import com.educoon.infra.ai.GeminiApiService;
import com.educoon.infra.ai.PdfParsingService;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.studyRoom.repository.StudyRoomRepository;
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
        String instructions = String.format(
                "다음 텍스트를 기반으로 \"%s\" 유형의 퀴즈 5개를 생성해줘. questionType 필드에는 반드시 \"%s\"를 넣어줘.",
                typeDescription, quizType.name()
        );

        return geminiApiService.generateJsonContent(text, instructions);
    }

    public Mono<String> processPdf(InputStream inputStream, String action, QuestionType quizType) {
        String text = pdfParsingService.extractText(inputStream);

        if ("summary".equals(action)) {
            return summarizeText(text);
        } else if ("quiz".equals(action)) {
            if (quizType == null) {
                return Mono.error(new IllegalArgumentException("Quiz type is required for quiz action"));
            }

            return quizText(text, quizType);
        } else {
            return Mono.error(new IllegalArgumentException("Invalid action type"));
        }
    }

    private Mono<String> recommendStudyRoom(String message) {
        // 1. 키워드 추출 (이 부분은 기존과 동일)
        String keywordPrompt = "다음 문장에서 핵심 검색 키워드 단어 1개만 추출해줘. 설명 없이 단어만 대답해. (예: '스프링부트 스터디 추천해줘' -> '스프링부트'): " + message;

        return geminiApiService.generateContent(keywordPrompt)
                .flatMap(keyword -> {
                    String trimmedKeyword = keyword.trim();
                    log.info("사용자 질문: {}", message);
                    log.info("AI 추출 키워드: '{}'", trimmedKeyword);

                    // 2. DB 검색 (N+1 해결된 메서드 사용)
                    List<StudyRoom> foundRooms = studyRoomRepository.findByKeywordWithTags(
                            trimmedKeyword, PageRequest.of(0, 5) // 5개 정도만
                    );

                    log.info("검색된 방 개수: {}", foundRooms.size());

                    String finalPrompt;

                    // =========================================================
                    // 💡 핵심 수정: 상황별 프롬프트 분리
                    // =========================================================

                    if (foundRooms.isEmpty()) {
                        // [CASE 1: 방이 없을 때] -> 위로와 조언, 방 만들기 권유
                        finalPrompt = String.format(
                                "사용자가 '%s'에 대한 스터디룸을 찾고 싶어 하는데, 현재 개설된 방이 하나도 없어.\n\n" +
                                        "**지시사항:**\n" +
                                        "1. 우선 아쉬움을 표현해줘.\n" +
                                        "2. '%s' 공부를 혼자 시작할 때 도움이 되는 팁이나 학습 로드맵을 간단히 알려줘.\n" +
                                        "3. 마지막에 '원하는 방이 없다면 직접 스터디룸을 만들어 팀원을 모집해보세요!'라고 격려해줘.",
                                trimmedKeyword, trimmedKeyword
                        );
                    }else {
                        // [CASE 2: 방이 있을 때] -> 상세 정보 포함
                        String roomList = foundRooms.stream()
                                .map(room -> {
                                    String tags = room.getRoomTagMaps().stream()
                                            .map(rtm -> "#" + rtm.getTag().getName())
                                            .collect(Collectors.joining(" "));

                                    // 1. 데이터 포맷을 명확하게 구조화
                                    return String.format(
                                            "[%s]\n- 태그: %s\n- 현황: %d명 / %d명 (참여/최대)\n- 방 소개: %s",
                                            room.getTitle(),
                                            tags.isEmpty() ? "(태그 없음)" : tags,
                                            room.getParticipants().size(),
                                            room.getMaxCapacity(),
                                            room.getDescription() != null ? room.getDescription() : "소개 없음"
                                    );
                                })
                                .collect(Collectors.joining("\n\n"));

                        // 2. 프롬프트 지시사항 구체화 (소개글, 인원 포함 강제)
                        finalPrompt = String.format(
                                "사용자가 요청한 키워드('%s')와 일치하는 스터디룸 목록이야:\n\n%s\n\n" +
                                        "**[매우 중요 - 엄격한 지시사항]**\n" +
                                        "1. **절대** 공부 방법이나 개념에 대해 설명하지 마. (TMI 금지)\n" +
                                        "2. 서론은 짧게 '회원님에게 딱 맞는 스터디룸을 찾았어요!' 정도로만 해.\n" +
                                        "3. 각 스터디룸을 추천할 때 **방 제목, 태그, 참여 현황(인원), 방 소개글**을 모두 포함해서 설명해줘.\n" + // [수정됨]
                                        "4. 사용자가 방을 선택할 수 있도록 구체적인 정보를 제공해줘.\n" + // [수정됨]
                                        "5. 다른 쓸데없는 말은 덧붙이지 마.",
                                trimmedKeyword, roomList
                        );
                    }

                    // 3. 최종 프롬프트로 AI 호출
                    return geminiApiService.generateContent(finalPrompt);
                });
    }
}
