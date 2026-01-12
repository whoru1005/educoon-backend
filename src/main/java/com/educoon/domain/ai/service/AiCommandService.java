package com.educoon.domain.ai.service;

import com.educoon.domain.chatMessage.entity.ChatMessage;
import com.educoon.domain.chatMessage.repository.ChatMessageRepository;
import com.educoon.domain.chatMessage.entity.MessageType;
import com.educoon.domain.chatMessage.WebSocketMessage;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.studyRoom.repository.StudyRoomRepository;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiCommandService {

    private final ChatMessageRepository chatMessageRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;
    private final ChatClient chatClient;
    private final SimpMessagingTemplate messagingTemplate;

    public AiCommandService(ChatMessageRepository chatMessageRepository, StudyRoomRepository studyRoomRepository, UserRepository userRepository, ChatClient.Builder chatClientBuilder, SimpMessagingTemplate messagingTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.studyRoomRepository = studyRoomRepository;
        this.userRepository = userRepository;
        this.chatClient = chatClientBuilder.build();
        this.messagingTemplate = messagingTemplate;
    }

    private static final Long AI_BOT_USER_ID = 1L;

    @Transactional
    public void executeCommand(Long roomId, String command) {
        if (command.contains("요약")) {
            summarizeChat(roomId);
        } else {
            log.info("알 수 없는 명령어: {}", command);
        }
    }

    private void summarizeChat(Long roomId) {
        // 1. 최근 채팅 50개 조회
        List<ChatMessage> recentMessages = chatMessageRepository.findTop50ByStudyRoomRoomIdOrderByTimestampDesc(roomId);

        if (recentMessages.isEmpty()) return;

        // 2. 프롬프트 생성
        String chatLog = recentMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getTimestamp))
                .map(msg -> msg.getUser().getNickname() + ": " + msg.getContent())
                .collect(Collectors.joining("\n"));

        String prompt = "다음 채팅 내역을 읽고, 3줄 이내로 현재 논의 중인 주제와 결론을 요약해줘.\n\n" + chatLog;

        // 3. ChatClient 호출
        Mono.fromCallable(() -> chatClient.prompt(prompt).call().content())
                .subscribe(summary -> {
                    // 4. 응답이 오면 봇이 말한 것처럼 방송
                    saveAndBroadcastAiMessage(roomId, " AI 요약: \n" + summary);
                }, error -> {
                    log.error("AI 요약 실패", error);
                });
    }

    private void saveAndBroadcastAiMessage(Long roomId, String content) {
        // AI 봇 유저 & 스터디룸 조회
        User aiBot = userRepository.findById(AI_BOT_USER_ID)
                .orElseThrow(() -> new CustomException(ErrorCode.AI_BOT_USER_NOT_FOUND));
        StudyRoom room = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND_FOR_AI));

        // 1. DB 저장
        ChatMessage aiMessage = ChatMessage.builder()
                .studyRoom(room)
                .user(aiBot)
                .content(content)
                .build();
        ChatMessage savedMessage = chatMessageRepository.save(aiMessage);

        // 2. WebSocket 방송 (MessageType.CHAT)
        WebSocketMessage wsMessage = WebSocketMessage.builder()
                .type(MessageType.CHAT)
                .userId(aiBot.getUserId())
                .nickname(aiBot.getNickname())
                .profileImageUrl(aiBot.getProfileImageUrl())
                .content(savedMessage.getContent())
                .timestamp(savedMessage.getTimestamp())
                .build();

        messagingTemplate.convertAndSend("/topic/studyrooms/" + roomId, wsMessage);
    }
}