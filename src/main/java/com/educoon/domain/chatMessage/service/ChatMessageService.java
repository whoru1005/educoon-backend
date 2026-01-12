package com.educoon.domain.chatMessage.service;

import com.educoon.domain.chatMessage.dto.ChatMessageRequest;
import com.educoon.domain.chatMessage.WebSocketMessage;
import com.educoon.domain.chatMessage.entity.ChatMessage;
import com.educoon.domain.chatMessage.entity.MessageType;
import com.educoon.domain.chatMessage.repository.ChatMessageRepository;
import com.educoon.domain.studyRoom.entity.StudyRoom;
import com.educoon.domain.studyRoom.service.StudyRoomService;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final UserService userService;
    private final StudyRoomService studyRoomService;
    private final ChatMessageRepository chatMessageRepository;

    public WebSocketMessage sendMessage(Long roomId, String kakaoId, ChatMessageRequest request){
        User sender = userService.findByKakaoId(kakaoId);
        StudyRoom studyRoom = studyRoomService.getStudyRoomById(roomId);

        ChatMessage newChatMessage = ChatMessage.builder()
                .studyRoom(studyRoom)
                .user(sender)
                .content(request.getContent())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(newChatMessage);
        log.debug("채팅 메시지 저장: roomId={}, sender={}", roomId, sender.getNickname());

        return WebSocketMessage.builder()
                .type(MessageType.CHAT)
                .userId(sender.getUserId())
                .nickname(sender.getNickname())
                .profileImageUrl(sender.getProfileImageUrl())
                .content(savedMessage.getContent())
                .timestamp(savedMessage.getTimestamp())
                .build();
    }
}
