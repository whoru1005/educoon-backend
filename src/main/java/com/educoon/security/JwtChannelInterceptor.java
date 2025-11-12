package com.educoon.security;

import com.educoon.exception.CustomException;
import com.educoon.exception.ErrorCode;
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
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor{

    private final JwtUtil jwtUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor stompHeaderAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(StompCommand.CONNECT.equals(stompHeaderAccessor.getCommand())){
            log.info("WebSocket Connect 요청 감지. JWT 검증 시작");

            String authHeader = stompHeaderAccessor.getFirstNativeHeader("Authorization");

            if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")){
                String jwt = authHeader.substring(7);

                if(jwtUtil.validateToken(jwt)){
                    Authentication authentication = jwtUtil.getAuthentication(jwt);
                    stompHeaderAccessor.setUser(authentication);
                    log.debug("Websocket 인증 성공. User: {}", authentication.getName());
                }else{
                    log.warn("WebSocket 인증 실패. 유효하지 않은 JWT");
                    throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
                }
            }else {
                log.warn("WebSocket 인증 실패. Authorization 헤더 없음");
                throw new CustomException(ErrorCode.INVALID_JWT_TOKEN);
            }
        }

        return message;
    }
}
