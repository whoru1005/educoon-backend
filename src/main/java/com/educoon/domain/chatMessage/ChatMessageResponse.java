package com.educoon.domain.chatMessage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatMessageResponse {

    private Long userId;
    private String nickName;
    private String content;
    private LocalDateTime timestamp;

}
