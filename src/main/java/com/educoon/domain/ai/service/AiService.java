package com.educoon.domain.ai.service;

import com.educoon.domain.quiz.dto.QuizQuestionDto;
import com.educoon.domain.quiz.entity.QuestionType;
import com.educoon.infra.ai.PdfParsingService;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.studyRoom.repository.StudyRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiService {

    private final ChatClient chatClient;
    private final PdfParsingService pdfParsingService;
    private final StudyRoomRepository studyRoomRepository;

    public AiService(ChatClient.Builder chatClientBuilder, PdfParsingService pdfParsingService, StudyRoomRepository studyRoomRepository) {
        this.chatClient = chatClientBuilder.build();
        this.pdfParsingService = pdfParsingService;
        this.studyRoomRepository = studyRoomRepository;
    }

    public Mono<String> chat(String message){
        if(message.contains("스터디") || message.contains("추천")){
            log.info("AI 스터디룸 추천 요청: message={}", message);
            return recommendStudyRoom(message);
        }else{
            log.debug("AI 일반 채팅 요청: message={}", message);
            return Mono.fromCallable(() -> chatClient.prompt(message).call().content());
        }
    }

    public Mono<String> summarizeText(String text){
        log.info("AI 텍스트 요약 요청 ({} 자)", text.length());
        String prompt = "다음 텍스트를 한국어로 요약해줘:\n\n" + text;
        return Mono.fromCallable(() -> chatClient.prompt(prompt).call().content());
    }

    public Mono<List<QuizQuestionDto>> quizText(String text, QuestionType quizType) {
        log.info("AI 퀴즈 생성 요청: type={}, ({} 자)", quizType, text.length());
        String typeDescription = switch (quizType) {
            case MULTIPLE_CHOICE -> "객관식(MULTIPLE_CHOICE)";
            case TRUE_FALSE      -> "OX(TRUE_FALSE)";
            case SHORT_ANSWER    -> "단답형(SHORT_ANSWER)";
        };

        String instructions = """
                다음 텍스트를 기반으로 {typeDescription} 유형의 퀴즈 5개를 생성해줘. 
                questionType 필드에는 반드시 {quizTypeName}을 넣어줘.
                
                텍스트:
                {text}
                """;

        BeanOutputConverter<List<QuizQuestionDto>> converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<QuizQuestionDto>>() {});

        return Mono.fromCallable(() -> {
            PromptTemplate promptTemplate = new PromptTemplate(instructions);
            Prompt prompt = promptTemplate.create(Map.of(
                    "typeDescription", typeDescription,
                    "quizTypeName", quizType.name(),
                    "text", text
            ));

            return chatClient.prompt(prompt)
                    .call()
                    .entity(converter);
        });
    }

    public Mono<?> processPdf(InputStream inputStream, String action, QuestionType quizType) {
        log.info("AI PDF 처리 시작: action={}, quizType={}", action, quizType);
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
        // 1. 키워드 추출
        String keywordPrompt = "다음 문장에서 핵심 검색 키워드 단어 1개만 추출해줘. 설명 없이 단어만 대답해. (예: '스프링부트 스터디 추천해줘' -> '스프링부트'): " + message;

        return Mono.fromCallable(() -> chatClient.prompt(keywordPrompt).call().content())
                .flatMap(keyword -> {
                    String trimmedKeyword = keyword.trim();
                    log.debug("사용자 질문: {}", message);
                    log.debug("AI 추출 키워드: '{}'", trimmedKeyword);

                    // 2. DB 검색 (N+1 해결된 메서드 사용)
                    List<StudyRoom> foundRooms = studyRoomRepository.findByKeywordWithTags(
                            trimmedKeyword, PageRequest.of(0, 5) // 5개 정도만
                    );

                    log.debug("검색된 방 개수: {}", foundRooms.size());

                    String finalPrompt;

                    if (foundRooms.isEmpty()) {
                        // [CASE 1: 방이 없을 때] -> 위로와 조언, 방 만들기 권유
                        finalPrompt = """
                            사용자가 '%s'에 대한 스터디룸을 찾고 싶어 하는데, 현재 개설된 방이 하나도 없어.
                            
                            **지시사항:**
                            1. 우선 아쉬움을 표현해줘.
                            2. '%s' 공부를 혼자 시작할 때 도움이 되는 팁이나 학습 로드맵을 간단히 알려줘.
                            3. 마지막에 '원하는 방이 없다면 직접 스터디룸을 만들어 팀원을 모집해보세요!'라고 격려해줘.
                            """.formatted(trimmedKeyword, trimmedKeyword);
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
                        finalPrompt = """
                            사용자가 요청한 키워드('%s')와 일치하는 스터디룸 목록이야:
                            
                            %s
                            
                            **[매우 중요 - 엄격한 지시사항]**
                            1. **절대** 공부 방법이나 개념에 대해 설명하지 마. (TMI 금지)
                            2. 서론은 짧게 '회원님에게 딱 맞는 스터디룸을 찾았어요!' 정도로만 해.
                            3. 각 스터디룸을 추천할 때 **방 제목, 태그, 참여 현황(인원), 방 소개글**을 모두 포함해서 설명해줘.
                            4. 사용자가 방을 선택할 수 있도록 구체적인 정보를 제공해줘.
                            5. 다른 쓸데없는 말은 덧붙이지 마.
                            """.formatted(trimmedKeyword, roomList);
                    }

                    // 3. 최종 프롬프트로 AI 호출
                    return Mono.fromCallable(() -> chatClient.prompt(finalPrompt).call().content());
                });
    }
}
