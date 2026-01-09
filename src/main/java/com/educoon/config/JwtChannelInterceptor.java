package com.educoon.config;

import com.educoon.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())){

            String authHeader = accessor.getFirstNativeHeader(AUTH_HEADER);
            if(authHeader != null && authHeader.startsWith(BEARER_PREFIX)){
                String token = authHeader.substring(BEARER_PREFIX.length());
                try {
                    Authentication authentication = jwtUtil.validateAndGetAuthentication(token);
                    if (authentication != null) {
                        accessor.setUser(authentication);
                        log.debug("WebSocket STOMP 연결 인증 성공: {}", authentication.getName());
                    } else {
                        log.warn("WebSocket STOMP 연결 인증 실패: 유효하지 않은 토큰");
                        return null; // (연결 거부)
                    }
                } catch (Exception e) {
                    log.warn("WebSocket STOMP connection failed: {}", e.getMessage());
                    return null; // (연결 거부)
                }
            }
        }

        return message;
    }
}
