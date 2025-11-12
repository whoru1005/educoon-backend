package com.educoon.domain.chatMessage;

import com.educoon.domain.studyRoom.StudyRoom;
import com.educoon.domain.studyRoom.StudyRoomRepository;
import com.educoon.domain.user.User;
import com.educoon.domain.user.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final UserRepository userRepository;

    @MessageMapping("/pub/studyrooms/{roomId}/chat")
    @Transactional
    public void sendChatMessage(
            @DestinationVariable Long roomId,
            ChatRequest chatRequest,
            SimpMessageHeaderAccessor headerAccessor){

        String kakaoId = Objects.requireNonNull(headerAccessor.getUser()).getName();
        User user = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        StudyRoom studyRoom = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        ChatMessage chatMessage = ChatMessage.builder()
                .studyRoom(studyRoom)
                .user(user)
                .content(chatRequest.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        chatMessageRepository.save(chatMessage);
        log.debug("채팅 DB 저장: roomId = {}, userId = {}, msg = {}", roomId, user.getNickname(), chatRequest.getMessage());

        ChatResponse chatResponse = ChatResponse.form(user, chatRequest.getMessage());

        messagingTemplate.convertAndSend("sub/studyrooms/" + roomId + "/chat" + chatResponse);

    }
}
