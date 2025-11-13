package com.educoon.domain.chatMessage;

import com.educoon.domain.studyRoom.StudyRoom;
import com.educoon.domain.studyRoom.StudyRoomRepository;
import com.educoon.domain.user.User;
import com.educoon.domain.user.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

    private final UserRepository userRepository;
    private final StudyRoomRepository studyRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    @MessageMapping("/chat/studyrooms/{roomId}/send")
    @SendTo("/topic/studyrooms/{roomId}")
    @Transactional
    public WebSocketMessage sendMesssage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor
    ){
        Authentication authentication = (Authentication) headerAccessor.getUser();
        if(authentication == null){
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        String kakaoId = authentication.getName();
        User sender = userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        StudyRoom studyRoom = studyRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROOM_NOT_FOUND));

        ChatMessage newChatMessage = ChatMessage.builder()
                .studyRoom(studyRoom)
                .user(sender)
                .content(request.getContent())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(newChatMessage);

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
