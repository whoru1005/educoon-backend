package com.educoon.domain.studyRoom.controller;

import com.educoon.config.SessionRoomRegistry;
import com.educoon.domain.chatMessage.entity.MessageType;
import com.educoon.domain.chatMessage.WebSocketMessage;
import com.educoon.domain.user.entity.User;
import com.educoon.domain.user.dto.UserLocation;
import com.educoon.domain.user.repository.UserRepository;
import com.educoon.domain.user.dto.UserStatus;
import com.educoon.domain.user.service.UserService;
import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
public class StudyRoomSocketController {

    private final UserService userService;
    private final SessionRoomRegistry sessionRoomRegistry;

    @MessageMapping("/studyrooms/{roomId}/join")
    @SendTo("/topic/studyrooms/{roomId}")
    @Transactional(readOnly = true)
    public WebSocketMessage userJoin(
            @DestinationVariable Long roomId,
            SimpMessageHeaderAccessor headerAccessor
    ){
        Authentication authentication = (Authentication) headerAccessor.getUser();
        User user = userService.findByKakaoId(authentication.getName());

        UserStatus newUserStatus = UserStatus.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .status(UserStatus.Status.IDLE)
                .build();

        sessionRoomRegistry.userJoin(roomId, newUserStatus);

        UserLocation userLocation = new UserLocation(user.getUserId(), user.getNickname(), roomId);
        sessionRoomRegistry.registerSession(headerAccessor.getSessionId(), userLocation);

        return WebSocketMessage.builder()
                .type(MessageType.JOIN)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    @MessageMapping("/studyrooms/{roomId}/start")
    @SendTo("/topic/studyrooms/{roomId}")
    @Transactional(readOnly = true)
    public WebSocketMessage focusStart(
            @DestinationVariable Long roomId,
            SimpMessageHeaderAccessor headerAccessor
    ){
        Authentication authentication = (Authentication) headerAccessor.getUser();
        User user = userService.findByKakaoId(authentication.getName());

        LocalDateTime startTime = LocalDateTime.now();

        UserStatus userStatus = sessionRoomRegistry.getUserStatus(roomId, user.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        userStatus.setStatus(UserStatus.Status.STUDYING);
        userStatus.setStudyStartTime(startTime);

        sessionRoomRegistry.userJoin(roomId, userStatus);

        return WebSocketMessage.builder()
                .type(MessageType.FOCUS_START)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .studyStartTime(startTime)
                .build();
    }

    @MessageMapping("/studyrooms/{roomId}/stop")
    @SendTo("/topic/studyrooms/{roomId}")
    @Transactional(readOnly = true)
    public WebSocketMessage focusStop(
            @DestinationVariable Long roomId,
            SimpMessageHeaderAccessor headerAccessor
    ){
        Authentication authentication = (Authentication) headerAccessor.getUser();
        User user = userService.findByKakaoId(authentication.getName());

        UserStatus userStatus = sessionRoomRegistry.getUserStatus(roomId, user.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        userStatus.setStatus(UserStatus.Status.IDLE);
        userStatus.setStudyStartTime(null);

        sessionRoomRegistry.userJoin(roomId, userStatus);

        return WebSocketMessage.builder()
                .type(MessageType.FOCUS_END)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .build();
    }
}
