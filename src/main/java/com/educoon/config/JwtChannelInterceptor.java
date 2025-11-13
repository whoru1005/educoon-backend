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

        log.warn("===== jwtUtil is null? : {} =====", jwtUtil == null);

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if(accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())){

            String authHeader = accessor.getFirstNativeHeader(AUTH_HEADER);
            System.out.println("11111");
            if(authHeader != null && authHeader.startsWith(BEARER_PREFIX)){
                String token = authHeader.substring(BEARER_PREFIX.length());
                System.out.println("SSSssss");
                try {
                    // 3. JwtUtil을 사용해 토큰 검증
                    if (jwtUtil.validateToken(token)) {
                        System.out.println("ddddd");
                        // 4. 토큰이 유효하면, Authentication 객체를 생성
                        Authentication authentication = jwtUtil.getAuthentication(token);
                        System.out.println("fffff");
                        // 5. [핵심] 웹소켓 세션에 인증 정보(Authentication)를 등록
                        accessor.setUser(authentication);
                        System.out.println("ppppp");
                        log.info("WebSocket STOMP connected, user: {}", authentication.getName());
                    }
                } catch (Exception e) {
                    // (토큰이 유효하지 않으면 연결 자체를 맺지 않음)
                    log.warn("WebSocket STOMP connection failed: {}", e.getMessage());
                    return null; // (연결 거부)
                }
            }
        }

        return message;
    }
}
