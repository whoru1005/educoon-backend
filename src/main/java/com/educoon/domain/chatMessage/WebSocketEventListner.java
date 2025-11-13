package com.educoon.domain.chatMessage;

import com.educoon.config.SessionRoomRegistry;
import com.educoon.domain.user.UserLocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListner {

    private final SessionRoomRegistry sessionRoomRegistry;

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketDisConnectListner(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        Optional<UserLocation> locationOpt = sessionRoomRegistry.unregisterSession(sessionId);

        if (locationOpt.isPresent()) {
            UserLocation location = locationOpt.get();
            Long roomId = location.getRoomId();
            Long userId = location.getUserId();
            String nickname = location.getNickname();

            log.info("User Disconnected : sessionId={} userId={}, nickname={}, roomId={}", sessionId, userId, nickname, roomId);

            sessionRoomRegistry.userLeave(roomId, userId);

            WebSocketMessage leaveMessage = WebSocketMessage.builder()
                    .type(MessageType.LEAVE)
                    .userId(userId)
                    .nickname(nickname)
                    .build();

            messagingTemplate.convertAndSend("/topic/studyrooms/" + roomId, leaveMessage);
        }else {
            log.warn("Disconnect event received for unkown session: {}", sessionId);
        }
    }
}
