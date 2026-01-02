package com.educoon.domain.chatMessage;

import com.educoon.domain.chatMessage.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class WebSocketMessage {

    private MessageType type;

    private Long userId;
    private String nickname;

    private String content;

    private LocalDateTime studyStartTime;

    private String profileImageUrl;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

}
