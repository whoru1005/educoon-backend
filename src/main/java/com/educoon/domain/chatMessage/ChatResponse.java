package com.educoon.domain.chatMessage;

import com.educoon.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ChatResponse {

    private Long userId;
    private String nickName;
    private String message;
    private LocalDateTime createdAt;

    public static ChatResponse form(User user, String message){
        return new ChatResponse(
                user.getUserId(),
                user.getNickname(),
                message,
                LocalDateTime.now()
        );
    }
}
