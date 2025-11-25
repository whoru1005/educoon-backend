package com.educoon.domain.chatMessage;

import com.educoon.domain.studyRoom.StudyRoom;
import com.educoon.domain.studyRoom.StudyRoomRepository;
import com.educoon.domain.user.User;
import com.educoon.domain.user.UserRepository;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GetMapping("/api/chat/studyrooms/{roomId}/messages")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getChatHistory(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        Page<ChatMessage> messagePage = chatMessageRepository.findAllByStudyRoomRoomIdOrderByTimestampDesc(roomId, pageable);

        // 2. DTO 변환
        List<WebSocketMessage> messages = messagePage.getContent().stream()
                .map(msg -> WebSocketMessage.builder()
                        .type(MessageType.CHAT)
                        .userId(msg.getUser().getUserId())
                        .nickname(msg.getUser().getNickname())
                        .profileImageUrl(msg.getUser().getProfileImageUrl())
                        .content(msg.getContent())
                        .timestamp(msg.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        // 3. 응답 (데이터 + 다음 페이지 존재 여부)
        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);
        response.put("isLast", messagePage.isLast()); // 더 가져올 게 없으면 true

        return ResponseEntity.ok(response);
    }
}
